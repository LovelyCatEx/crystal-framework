export const ProjectDisplayName = "ManagerTemplate"
export const DocumentTitleDelimiter = " - "

export function buildDocumentTitle(
    mainName: string,
    delimiter: string = DocumentTitleDelimiter,
    displayName: string = ProjectDisplayName,
) {
    return `${mainName}${delimiter}${displayName}`
}