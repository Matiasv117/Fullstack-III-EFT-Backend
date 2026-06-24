import { useState, useCallback, useRef } from 'react';

let globalId = 0;

export function useToast() {
  const [toasts, setToasts] = useState([]);
  const timersRef = useRef({});

  const removeToast = useCallback((id) => {
    // First mark as exiting for animation
    setToasts((prev) => prev.map((t) => (t.id === id ? { ...t, exiting: true } : t)));
    // Then remove after animation
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 300);
    if (timersRef.current[id]) {
      clearTimeout(timersRef.current[id]);
      delete timersRef.current[id];
    }
  }, []);

  const addToast = useCallback(
    (message, type = 'info', duration = 4000) => {
      const id = ++globalId;
      setToasts((prev) => [...prev, { id, message, type, exiting: false }]);

      if (duration > 0) {
        timersRef.current[id] = setTimeout(() => {
          removeToast(id);
        }, duration);
      }

      return id;
    },
    [removeToast]
  );

  const success = useCallback((msg, dur) => addToast(msg, 'success', dur), [addToast]);
  const error = useCallback((msg, dur) => addToast(msg, 'error', dur), [addToast]);
  const warning = useCallback((msg, dur) => addToast(msg, 'warning', dur), [addToast]);
  const info = useCallback((msg, dur) => addToast(msg, 'info', dur), [addToast]);

  return { toasts, addToast, removeToast, success, error, warning, info };
}
