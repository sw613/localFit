import React from 'react';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';

const RelativeTime = ({ date }) => {
  return (
    <span>
      {formatDistanceToNow(new Date(date), { addSuffix: true, locale: ko })}
    </span>
  );
};

export default RelativeTime;
