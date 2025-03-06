import React from 'react';
import { useDrag, useDrop } from 'react-dnd';

const ITEM_TYPE = 'IMAGE_ITEM';

function ImageItem({ url, index, moveImage }) {
  // 드래그 설정
  const [{ isDragging }, dragRef] = useDrag(() => ({
    type: ITEM_TYPE,
    item: { index },  
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  }));

  const [{ isOver }, dropRef] = useDrop(() => ({
    accept: ITEM_TYPE,
    drop: (draggedItem) => {
      if (draggedItem.index === index) return;
      moveImage(draggedItem.index, index);
      draggedItem.index = index;
    },
    collect: (monitor) => ({
      isOver: monitor.isOver(),
    }),
  }));

  const opacity = isDragging ? 0.5 : 1;

  return (
    <div
      ref={(node) => dragRef(dropRef(node))}
      style={{
        opacity,
        border: '1px solid #ccc',
        borderRadius: '5px',
        marginBottom: '8px',
        cursor: 'move',
        backgroundColor: isOver ? '#f9f9f9' : 'white',
      }}
    >
      <img
        src={url}
        alt="preview"
        style={{ width: '80px', height: '80px', objectFit: 'cover', display: 'block' }}
      />
    </div>
  );
}

export default ImageItem;
