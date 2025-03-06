import React, { useState } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';

function MyEditor() {
  const [editorContents, setEditorContents] = useState('');

  const handleEditorChange = (content, delta, source, editor) => {
    setEditorContents(content);
  };

  return (
    <div>
      <h2>Quill 에디터 예시</h2>
      <ReactQuill 
        theme="snow"              
        value={editorContents}    
        onChange={handleEditorChange} 
      />
      <p>에디터 내용 미리보기:</p>
      <div dangerouslySetInnerHTML={{ __html: editorContents }} />
    </div>
  );
}

export default MyEditor;
