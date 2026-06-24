import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

const colorMap = {
  success: {
    bg: 'bg-emerald-50 dark:bg-emerald-950/40',
    border: 'border-emerald-200 dark:border-emerald-800',
    icon: 'text-emerald-600 dark:text-emerald-400',
    text: 'text-emerald-900 dark:text-emerald-200',
    progress: 'bg-emerald-500',
  },
  error: {
    bg: 'bg-rose-50 dark:bg-rose-950/40',
    border: 'border-rose-200 dark:border-rose-800',
    icon: 'text-rose-600 dark:text-rose-400',
    text: 'text-rose-900 dark:text-rose-200',
    progress: 'bg-rose-500',
  },
  warning: {
    bg: 'bg-amber-50 dark:bg-amber-950/40',
    border: 'border-amber-200 dark:border-amber-800',
    icon: 'text-amber-600 dark:text-amber-400',
    text: 'text-amber-900 dark:text-amber-200',
    progress: 'bg-amber-500',
  },
  info: {
    bg: 'bg-blue-50 dark:bg-blue-950/40',
    border: 'border-blue-200 dark:border-blue-800',
    icon: 'text-blue-600 dark:text-blue-400',
    text: 'text-blue-900 dark:text-blue-200',
    progress: 'bg-blue-500',
  },
};

function ToastItem({ toast, onRemove }) {
  const colors = colorMap[toast.type] || colorMap.info;
  const Icon = iconMap[toast.type] || Info;

  return (
    <div
      className={`
        flex items-start gap-3 p-4 rounded-xl border shadow-lg backdrop-blur-sm
        ${colors.bg} ${colors.border}
        ${toast.exiting ? 'animate-slide-out-right' : 'animate-slide-in-right'}
        max-w-sm w-full pointer-events-auto
      `}
      role="alert"
    >
      <Icon className={`w-5 h-5 shrink-0 mt-0.5 ${colors.icon}`} />
      <p className={`text-sm font-medium flex-1 ${colors.text}`}>{toast.message}</p>
      <button
        onClick={() => onRemove(toast.id)}
        className={`shrink-0 p-0.5 rounded-md hover:bg-black/5 dark:hover:bg-white/10 transition-colors ${colors.icon}`}
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
}

function ToastContainer({ toasts, onRemove }) {
  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-20 right-6 z-[100] flex flex-col gap-3 pointer-events-none">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onRemove={onRemove} />
      ))}
    </div>
  );
}

export default ToastContainer;
