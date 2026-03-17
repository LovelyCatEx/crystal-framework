import React, { useRef, useState, useCallback, useEffect } from 'react';
import { Modal, Slider, Button, Space } from 'antd';
import { ZoomInOutlined, ZoomOutOutlined, RotateLeftOutlined, RotateRightOutlined, ReloadOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';

export type CropShape = 'rect' | 'circle';

export interface ImageCropperProps {
  open: boolean;
  imageUrl: string;
  onCancel: () => void;
  onConfirm: (croppedImage: Blob) => void;
  aspectRatio?: number;
  shape?: CropShape;
  title?: string;
  confirmText?: string;
  cancelText?: string;
  minZoom?: number;
  maxZoom?: number;
  quality?: number;
}

interface Point {
  x: number;
  y: number;
}

interface CropState {
  scale: number;
  rotation: number;
  position: Point;
  isDragging: boolean;
  dragStart: Point;
}

// Inner cropper component
interface ImageCropperInnerProps extends ImageCropperProps {
  componentKey: number;
}

const ImageCropperInner: React.FC<ImageCropperInnerProps> = ({
  open,
  imageUrl,
  onCancel,
  onConfirm,
  aspectRatio = 1,
  shape = 'rect',
  title,
  confirmText,
  cancelText,
  minZoom = 0.5,
  maxZoom = 3,
  quality = 0.9,
}) => {
  const { t } = useTranslation();
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const imageRef = useRef<HTMLImageElement | null>(null);
  const displayImageRef = useRef<HTMLImageElement | null>(null);
  const initialScaleRef = useRef(1);

  const [cropState, setCropState] = useState<CropState>({
    scale: 1,
    rotation: 0,
    position: { x: 0, y: 0 },
    isDragging: false,
    dragStart: { x: 0, y: 0 },
  });

  const [imageLoaded, setImageLoaded] = useState(false);
  const [containerSize, setContainerSize] = useState({ width: 0, height: 0 });

  // Load image and calculate initial scale
  useEffect(() => {
    if (!open || !imageUrl) return;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      imageRef.current = img;

      // Wait for container size to be determined before calculating
      setTimeout(() => {
        if (!containerRef.current) return;

        const containerRect = containerRef.current.getBoundingClientRect();
        const containerW = containerRect.width;
        const containerH = containerRect.height;

        // Calculate actual crop area size
        const minSize = Math.min(containerW, containerH) - 40;
        let cropW = minSize;
        let cropH = minSize;

        if (aspectRatio && aspectRatio !== 1) {
          if (aspectRatio > 1) {
            cropH = cropW / aspectRatio;
          } else {
            cropW = cropH * aspectRatio;
          }
        }

        // Base display height (display height when scale=1)
        const baseDisplayHeight = 300;
        const imgAspectRatio = img.naturalWidth / img.naturalHeight;
        const baseDisplayWidth = baseDisplayHeight * imgAspectRatio;

        // Calculate required scale to make image display size >= crop area size
        const scaleX = cropW / baseDisplayWidth;
        const scaleY = cropH / baseDisplayHeight;

        // Take the larger value to ensure image fills the crop area
        let initialScale = Math.max(scaleX, scaleY);

        // Clamp initial scale between minZoom and maxZoom
        initialScale = Math.max(initialScale, minZoom);
        initialScale = Math.min(initialScale, maxZoom);

        initialScaleRef.current = initialScale;
        setCropState({
          scale: initialScale,
          rotation: 0,
          position: { x: 0, y: 0 },
          isDragging: false,
          dragStart: { x: 0, y: 0 },
        });
        setContainerSize({ width: containerW, height: containerH });
        setImageLoaded(true);
      }, 0);
    };
    img.src = imageUrl;

    return () => {
      img.onload = null;
    };
  }, [open, imageUrl, aspectRatio, minZoom, maxZoom]);

  // Calculate crop area size
  const getCropAreaSize = useCallback(() => {
    const minSize = Math.min(containerSize.width, containerSize.height) - 40;
    let width = minSize;
    let height = minSize;

    if (aspectRatio && aspectRatio !== 1) {
      if (aspectRatio > 1) {
        height = width / aspectRatio;
      } else {
        width = height * aspectRatio;
      }
    }

    return { width, height };
  }, [containerSize, aspectRatio]);

  // Calculate image display size in container (maintaining aspect ratio)
  const getImageDisplaySize = useCallback((scale: number) => {
    if (!imageRef.current) return { width: 0, height: 0 };

    const img = imageRef.current;
    const maxDisplayHeight = 300;

    const imgAspectRatio = img.naturalWidth / img.naturalHeight;
    const displayHeight = maxDisplayHeight * scale;
    const displayWidth = displayHeight * imgAspectRatio;

    return { width: displayWidth, height: displayHeight };
  }, []);

  // Clamp position within image bounds
  const clampPosition = useCallback((pos: Point, scale: number, rotation: number): Point => {
    if (!imageRef.current) return pos;

    const cropSize = getCropAreaSize();
    const imgSize = getImageDisplaySize(scale);

    // Calculate image bounds after rotation
    const rotationRad = (rotation * Math.PI) / 180;
    const cos = Math.abs(Math.cos(rotationRad));
    const sin = Math.abs(Math.sin(rotationRad));

    // Rotated image bounding box
    const rotatedImgWidth = imgSize.width * cos + imgSize.height * sin;
    const rotatedImgHeight = imgSize.width * sin + imgSize.height * cos;

    // Calculate maximum allowed offset
    let limitX = (rotatedImgWidth - cropSize.width) / 2;
    let limitY = (rotatedImgHeight - cropSize.height) / 2;

    // Use simple limits when not rotated
    if (rotation % 180 === 0) {
      limitX = (imgSize.width - cropSize.width) / 2;
      limitY = (imgSize.height - cropSize.height) / 2;
    } else if (rotation % 180 === 90) {
      limitX = (imgSize.height - cropSize.width) / 2;
      limitY = (imgSize.width - cropSize.height) / 2;
    }

    // Ensure crop area does not exceed image bounds
    limitX = Math.max(0, limitX);
    limitY = Math.max(0, limitY);

    return {
      x: Math.max(-limitX, Math.min(limitX, pos.x)),
      y: Math.max(-limitY, Math.min(limitY, pos.y)),
    };
  }, [getCropAreaSize, getImageDisplaySize]);

  // Handle drag start
  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    setCropState(prev => ({
      ...prev,
      isDragging: true,
      dragStart: {
        x: e.clientX - prev.position.x,
        y: e.clientY - prev.position.y,
      },
    }));
  }, []);

  // Handle drag move
  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    setCropState(prev => {
      if (!prev.isDragging) return prev;

      const newPosition = {
        x: e.clientX - prev.dragStart.x,
        y: e.clientY - prev.dragStart.y,
      };

      const clampedPosition = clampPosition(newPosition, prev.scale, prev.rotation);

      return {
        ...prev,
        position: clampedPosition,
      };
    });
  }, [clampPosition]);

  // Handle drag end
  const handleMouseUp = useCallback(() => {
    setCropState(prev => ({ ...prev, isDragging: false }));
  }, []);

  // Handle touch events
  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    const touch = e.touches[0];
    setCropState(prev => ({
      ...prev,
      isDragging: true,
      dragStart: {
        x: touch.clientX - prev.position.x,
        y: touch.clientY - prev.position.y,
      },
    }));
  }, []);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    setCropState(prev => {
      if (!prev.isDragging) return prev;

      const touch = e.touches[0];
      const newPosition = {
        x: touch.clientX - prev.dragStart.x,
        y: touch.clientY - prev.dragStart.y,
      };

      const clampedPosition = clampPosition(newPosition, prev.scale, prev.rotation);

      return {
        ...prev,
        position: clampedPosition,
      };
    });
  }, [clampPosition]);

  const handleTouchEnd = useCallback(() => {
    setCropState(prev => ({ ...prev, isDragging: false }));
  }, []);

  // Zoom controls
  const handleZoomChange = useCallback((value: number) => {
    setCropState(prev => {
      const newPosition = clampPosition(prev.position, value, prev.rotation);
      return { ...prev, scale: value, position: newPosition };
    });
  }, [clampPosition]);

  const handleZoomIn = useCallback(() => {
    setCropState(prev => {
      const newScale = Math.min(prev.scale + 0.1, maxZoom);
      const newPosition = clampPosition(prev.position, newScale, prev.rotation);
      return { ...prev, scale: newScale, position: newPosition };
    });
  }, [maxZoom, clampPosition]);

  const handleZoomOut = useCallback(() => {
    setCropState(prev => {
      const newScale = Math.max(prev.scale - 0.1, minZoom);
      const newPosition = clampPosition(prev.position, newScale, prev.rotation);
      return { ...prev, scale: newScale, position: newPosition };
    });
  }, [minZoom, clampPosition]);

  // Rotation controls
  const handleRotateLeft = useCallback(() => {
    setCropState(prev => {
      const newRotation = prev.rotation - 90;
      const newPosition = clampPosition(prev.position, prev.scale, newRotation);
      return { ...prev, rotation: newRotation, position: newPosition };
    });
  }, [clampPosition]);

  const handleRotateRight = useCallback(() => {
    setCropState(prev => {
      const newRotation = prev.rotation + 90;
      const newPosition = clampPosition(prev.position, prev.scale, newRotation);
      return { ...prev, rotation: newRotation, position: newPosition };
    });
  }, [clampPosition]);

  // Reset to initial state
  const handleReset = useCallback(() => {
    setCropState(prev => {
      const newPosition = clampPosition({ x: 0, y: 0 }, initialScaleRef.current, 0);
      return {
        ...prev,
        scale: initialScaleRef.current,
        rotation: 0,
        position: newPosition,
      };
    });
  }, [clampPosition]);

  // Perform crop using original image for high quality output
  const performCrop = useCallback(() => {
    if (!imageRef.current) return;

    const img = imageRef.current;
    const cropSize = getCropAreaSize();

    // Calculate the scale ratio between original image and display
    // Display base height is 300px when scale=1
    const baseDisplayHeight = 300;
    const displayToOriginalRatio = img.naturalHeight / baseDisplayHeight;

    // Calculate output dimensions based on original image resolution
    const outputWidth = Math.round(cropSize.width * displayToOriginalRatio / cropState.scale);
    const outputHeight = Math.round(cropSize.height * displayToOriginalRatio / cropState.scale);

    // Create output canvas with original image resolution
    const outputCanvas = document.createElement('canvas');
    outputCanvas.width = outputWidth;
    outputCanvas.height = outputHeight;
    const outputCtx = outputCanvas.getContext('2d');
    if (!outputCtx) return;

    // Clear canvas
    outputCtx.clearRect(0, 0, outputCanvas.width, outputCanvas.height);

    // Save context
    outputCtx.save();

    // Move to center of output canvas
    outputCtx.translate(outputWidth / 2, outputHeight / 2);

    // Apply the same transforms as display, but scaled to original image resolution
    // Position needs to be scaled to original image coordinates
    const scaledPositionX = cropState.position.x * displayToOriginalRatio / cropState.scale;
    const scaledPositionY = cropState.position.y * displayToOriginalRatio / cropState.scale;

    outputCtx.translate(scaledPositionX, scaledPositionY);
    outputCtx.rotate((cropState.rotation * Math.PI) / 180);

    // Draw original image at full resolution (no additional scaling needed since we're using original)
    outputCtx.drawImage(
      img,
      -img.naturalWidth / 2,
      -img.naturalHeight / 2,
      img.naturalWidth,
      img.naturalHeight
    );

    outputCtx.restore();

    // Apply circular mask if shape is circle
    if (shape === 'circle') {
      outputCtx.globalCompositeOperation = 'destination-in';
      outputCtx.beginPath();
      outputCtx.arc(
        outputCanvas.width / 2,
        outputCanvas.height / 2,
        Math.min(outputCanvas.width, outputCanvas.height) / 2,
        0,
        Math.PI * 2
      );
      outputCtx.fill();
      outputCtx.globalCompositeOperation = 'source-over';
    }

    // Export as JPEG for better compatibility
    outputCanvas.toBlob(
      (blob) => {
        if (blob) {
          onConfirm(blob);
        }
      },
      'image/jpeg',
      quality
    );
  }, [cropState, getCropAreaSize, shape, quality, onConfirm]);

  const cropSize = getCropAreaSize();
  const cropAreaLeft = (containerSize.width - cropSize.width) / 2;
  const cropAreaTop = (containerSize.height - cropSize.height) / 2;

  return (
    <Modal
      open={open}
      title={title}
      onCancel={onCancel}
      width={600}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          {cancelText}
        </Button>,
        <Button key="confirm" type="primary" onClick={performCrop} disabled={!imageLoaded}>
          {confirmText}
        </Button>,
      ]}
    >
      <div className="flex flex-col items-center space-y-4 py-4">
        <div
          ref={containerRef}
          className="relative w-full h-[400px] bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center"
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
        >
          {imageLoaded && (
            <>
              {/* Hidden canvas for sync drawing */}
              <canvas
                ref={canvasRef}
                className="absolute inset-0 w-full h-full"
                style={{ opacity: 0, pointerEvents: 'none' }}
              />

              {/* Display image */}
              <div
                ref={(el) => {
                  if (el) {
                    const img = el.querySelector('img');
                    if (img) displayImageRef.current = img as HTMLImageElement;
                  }
                }}
                className="absolute cursor-move"
                style={{
                  transform: `
                    translate(${cropState.position.x}px, ${cropState.position.y}px)
                    rotate(${cropState.rotation}deg)
                    scale(${cropState.scale})
                  `,
                  transformOrigin: 'center center',
                }}
                onMouseDown={handleMouseDown}
                onTouchStart={handleTouchStart}
                onTouchMove={handleTouchMove}
                onTouchEnd={handleTouchEnd}
              >
                <img
                  src={imageUrl}
                  alt="Crop"
                  className="max-w-none select-none"
                  style={{ maxHeight: '300px' }}
                  draggable={false}
                />
              </div>

              {/* Overlay */}
              <div className="absolute inset-0 pointer-events-none">
                <svg className="absolute inset-0 w-full h-full">
                  <defs>
                    <mask id="cropMask">
                      <rect width="100%" height="100%" fill="white" />
                      {shape === 'circle' ? (
                        <circle
                          cx={containerSize.width / 2}
                          cy={containerSize.height / 2}
                          r={cropSize.width / 2}
                          fill="black"
                        />
                      ) : (
                        <rect
                          x={cropAreaLeft}
                          y={cropAreaTop}
                          width={cropSize.width}
                          height={cropSize.height}
                          fill="black"
                        />
                      )}
                    </mask>
                  </defs>
                  <rect
                    width="100%"
                    height="100%"
                    fill="rgba(0, 0, 0, 0.5)"
                    mask="url(#cropMask)"
                  />
                </svg>

                <div
                  className={`absolute border-2 border-white shadow-lg ${
                    shape === 'circle' ? 'rounded-full' : ''
                  }`}
                  style={{
                    width: cropSize.width,
                    height: cropSize.height,
                    left: cropAreaLeft,
                    top: cropAreaTop,
                    boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.5)',
                  }}
                >
                  <div className="absolute inset-0 opacity-50">
                    <div className="absolute top-1/3 left-0 right-0 h-px bg-white" />
                    <div className="absolute top-2/3 left-0 right-0 h-px bg-white" />
                    <div className="absolute left-1/3 top-0 bottom-0 w-px bg-white" />
                    <div className="absolute left-2/3 top-0 bottom-0 w-px bg-white" />
                  </div>
                </div>
              </div>
            </>
          )}

          {!imageLoaded && (
            <div className="text-gray-400">{t('components.imageCropper.loading')}</div>
          )}
        </div>

        <div className="w-full space-y-3">
          <div className="flex items-center space-x-3">
            <Button
              icon={<ZoomOutOutlined />}
              size="small"
              onClick={handleZoomOut}
              disabled={cropState.scale <= minZoom}
            />
            <Slider
              className="flex-1"
              min={minZoom}
              max={maxZoom}
              step={0.01}
              value={cropState.scale}
              onChange={handleZoomChange}
              tooltip={{ formatter: (value) => `${Math.round((value || 1) * 100)}%` }}
            />
            <Button
              icon={<ZoomInOutlined />}
              size="small"
              onClick={handleZoomIn}
              disabled={cropState.scale >= maxZoom}
            />
          </div>

          <Space>
            <Button
              icon={<RotateLeftOutlined />}
              size="small"
              onClick={handleRotateLeft}
            >
              {t('components.imageCropper.rotateLeft')}
            </Button>
            <Button
              icon={<RotateRightOutlined />}
              size="small"
              onClick={handleRotateRight}
            >
              {t('components.imageCropper.rotateRight')}
            </Button>
            <Button
              icon={<ReloadOutlined />}
              size="small"
              onClick={handleReset}
              disabled={!imageLoaded}
            >
              {t('components.imageCropper.reset')}
            </Button>
          </Space>
        </div>
      </div>
    </Modal>
  );
};

export const ImageCropper: React.FC<ImageCropperProps & { componentKey?: number; key?: number }> = ({
  componentKey = 0,
  ...props
}) => {
  return <ImageCropperInner componentKey={componentKey} {...props} />;
};

export default ImageCropper;
