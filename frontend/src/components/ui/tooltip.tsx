import * as TooltipPrimitive from "@radix-ui/react-tooltip";
import type { PropsWithChildren } from "react";

export function Tooltip({ children }: PropsWithChildren) {
  return <TooltipPrimitive.Provider delayDuration={100}>{children}</TooltipPrimitive.Provider>;
}

export const TooltipRoot = TooltipPrimitive.Root;
export const TooltipTrigger = TooltipPrimitive.Trigger;

export function TooltipContent({ children }: PropsWithChildren) {
  return (
    <TooltipPrimitive.Portal>
      <TooltipPrimitive.Content
        sideOffset={8}
        className="rounded-2xl bg-slate-950 px-3 py-2 text-xs font-medium text-white shadow-soft"
      >
        {children}
      </TooltipPrimitive.Content>
    </TooltipPrimitive.Portal>
  );
}
