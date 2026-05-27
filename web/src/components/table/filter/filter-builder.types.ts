/**
 * Developer-facing: what fields the user can filter on.
 */
export interface FilterableField {
  field: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'date' | 'dateTime';
  options?: { label: string; value: string | number }[];
}

/** Operator metadata used to render the operator selector. */
export interface OperatorOption {
  value: string;
  label: string;
}

/** Map of field type → available operators. */
export const FIELD_OPERATORS: Record<FilterableField['type'], OperatorOption[]> = {
  text: [
    { value: 'eq', label: 'components.filterBuilder.operators.eq' },
    { value: 'ne', label: 'components.filterBuilder.operators.ne' },
    { value: 'contains', label: 'components.filterBuilder.operators.contains' },
    { value: 'like', label: 'components.filterBuilder.operators.like' },
    { value: 'in', label: 'components.filterBuilder.operators.in' },
  ],
  number: [
    { value: 'eq', label: 'components.filterBuilder.operators.eq' },
    { value: 'ne', label: 'components.filterBuilder.operators.ne' },
    { value: 'gt', label: 'components.filterBuilder.operators.gt' },
    { value: 'gte', label: 'components.filterBuilder.operators.gte' },
    { value: 'lt', label: 'components.filterBuilder.operators.lt' },
    { value: 'lte', label: 'components.filterBuilder.operators.lte' },
    { value: 'in', label: 'components.filterBuilder.operators.in' },
  ],
  select: [
    { value: 'eq', label: 'components.filterBuilder.operators.eq' },
    { value: 'ne', label: 'components.filterBuilder.operators.ne' },
    { value: 'in', label: 'components.filterBuilder.operators.in' },
  ],
  date: [
    { value: 'eq', label: 'components.filterBuilder.operators.eq' },
    { value: 'ne', label: 'components.filterBuilder.operators.ne' },
    { value: 'gt', label: 'components.filterBuilder.operators.gt' },
    { value: 'gte', label: 'components.filterBuilder.operators.gte' },
    { value: 'lt', label: 'components.filterBuilder.operators.lt' },
    { value: 'lte', label: 'components.filterBuilder.operators.lte' },
  ],
  dateTime: [
    { value: 'eq', label: 'components.filterBuilder.operators.eq' },
    { value: 'ne', label: 'components.filterBuilder.operators.ne' },
    { value: 'gt', label: 'components.filterBuilder.operators.gt' },
    { value: 'gte', label: 'components.filterBuilder.operators.gte' },
    { value: 'lt', label: 'components.filterBuilder.operators.lt' },
    { value: 'lte', label: 'components.filterBuilder.operators.lte' },
  ],
};

/**
 * Internal editor state — a single condition row.
 */
export interface FilterRowState {
  id: string;
  field: string | null;
  operator: string | null;
  value: unknown;
}

/**
 * Internal editor state — a group of conditions / nested groups.
 */
export interface FilterGroupState {
  id: string;
  logic: 'and' | 'or';
  children: FilterNodeState[];
}

export type FilterNodeState = FilterRowState | FilterGroupState;

/**
 * Backend-facing query node — sent in the JSON body.
 */
export interface ConditionNode {
  type: 'condition';
  field: string;
  operator: string;
  value?: unknown;
  values?: unknown[];
}

export interface GroupNode {
  type: 'group';
  logic: 'and' | 'or';
  children: (GroupNode | ConditionNode)[];
}

// ── helpers ────────────────────────────────────────

let _id = 0;
export function generateId(): string {
  return `f_${++_id}_${Date.now().toString(36)}`;
}

export function createEmptyGroup(logic: 'and' | 'or' = 'and'): FilterGroupState {
  return { id: generateId(), logic, children: [] };
}

export function createEmptyRow(): FilterRowState {
  return { id: generateId(), field: null, operator: null, value: null };
}

export function isGroupNode(node: FilterNodeState): node is FilterGroupState {
  return 'logic' in node;
}

export function findFieldDef(fields: FilterableField[], fieldName: string | null): FilterableField | undefined {
  return fields.find(f => f.field === fieldName);
}

/** Get operators available for a given field definition. */
export function getFieldOperators(fieldDef: FilterableField | undefined): OperatorOption[] {
  return fieldDef ? FIELD_OPERATORS[fieldDef.type] : FIELD_OPERATORS.text;
}

// ── tree mutation helpers ──────────────────────────

function cloneNode(node: FilterNodeState): FilterNodeState {
  if (isGroupNode(node)) {
    return { ...node, children: node.children.map(cloneNode) };
  }
  return { ...node };
}

export function updateNodeById(
  group: FilterGroupState,
  id: string,
  updater: (node: FilterNodeState) => FilterNodeState,
): FilterGroupState {
  return {
    ...group,
    children: group.children.map(child => {
      if (child.id === id) return updater(cloneNode(child));
      if (isGroupNode(child)) return updateNodeById(child, id, updater);
      return child;
    }),
  };
}

export function removeNodeById(group: FilterGroupState, id: string): FilterGroupState {
  return {
    ...group,
    children: group.children
      .filter(child => child.id !== id)
      .map(child => (isGroupNode(child) ? removeNodeById(child, id) : child)),
  };
}

export function addRowToGroup(group: FilterGroupState): FilterGroupState {
  return { ...group, children: [...group.children, createEmptyRow()] };
}

export function addGroupToGroup(group: FilterGroupState): FilterGroupState {
  return { ...group, children: [...group.children, createEmptyGroup('and')] };
}

/** Convert internal editor tree → backend GroupNode. */
export function toGroupNode(group: FilterGroupState): GroupNode {
  return {
    type: 'group',
    logic: group.logic,
    children: group.children
      .map(child => {
        if (isGroupNode(child)) return toGroupNode(child);
        if (child.field && child.operator) {
          const cond: ConditionNode = { type: 'condition', field: child.field, operator: child.operator };
          if (child.operator === 'in' && Array.isArray(child.value)) {
            cond.values = child.value;
          } else {
            cond.value = child.value;
          }
          return cond;
        }
        return null;
      })
      .filter((n): n is GroupNode | ConditionNode => n !== null),
  };
}

/** Convert backend GroupNode → internal editor state (for URL restore). */
export function fromGroupNode(node: GroupNode): FilterGroupState {
  return {
    id: generateId(),
    logic: node.logic,
    children: node.children.map(child => {
      if (child.type === 'group') return fromGroupNode(child);
      return {
        id: generateId(),
        field: child.field,
        operator: child.operator,
        value: child.operator === 'in' ? (child.values ?? []) : child.value,
      } as FilterRowState;
    }),
  };
}

/** Recursively check if any node has missing/incomplete data that would block Apply. */
export function hasMissingValues(group: FilterGroupState): boolean {
  return group.children.some(child => {
    if (isGroupNode(child)) {
      if (child.children.length === 0) return true;           // empty group
      return hasMissingValues(child);
    }
    if (!child.field || !child.operator) return true;          // field or operator not set
    if (child.operator === 'in') return !Array.isArray(child.value) || child.value.length === 0;
    return child.value === null || child.value === undefined || child.value === '';  // value not filled
  });
}

/** Whether the tree has at least one completed condition row. */
export function hasActiveConditions(group: FilterGroupState): boolean {
  if (group.children.length === 0) return false;
  return group.children.some(child => {
    if (isGroupNode(child)) return hasActiveConditions(child);
    return child.field !== null && child.operator !== null;
  });
}

/** Count completed condition rows (for the badge number on the Filters button). */
export function getActiveConditionCount(group: FilterGroupState): number {
  let count = 0;
  for (const child of group.children) {
    if (isGroupNode(child)) {
      count += getActiveConditionCount(child);
    } else if (child.field !== null && child.operator !== null) {
      count++;
    }
  }
  return count;
}
