import { cn } from "@/lib/utils";

type CardProps = React.HTMLAttributes<HTMLDivElement>;

export function Card({ className, ...props }: CardProps) {
  return (
    <div
      className={cn(
        "glass-panel rounded-4xl border border-white/60 bg-white/75 p-6 shadow-card backdrop-blur-xl",
        className,
      )}
      {...props}
    />
  );
}
