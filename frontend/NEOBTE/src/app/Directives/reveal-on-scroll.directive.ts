import { Directive, ElementRef, Input, OnDestroy, OnInit } from '@angular/core';

@Directive({
  selector: '[revealOnScroll]',
  standalone: true,
})
export class RevealOnScrollDirective implements OnInit, OnDestroy {
  /** Intersection threshold (0–1). */
  @Input() revealThreshold = 0.14;
  /** Root margin for earlier reveal (e.g. "0px 0px -10% 0px"). */
  @Input() revealRootMargin = '0px 0px -10% 0px';
  /** Reveal only once. */
  @Input() revealOnce = true;

  private io?: IntersectionObserver;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    const node = this.el.nativeElement;
    node.classList.add('neo-reveal');

    // Reduced motion: show instantly.
    try {
      if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        node.classList.add('neo-reveal--visible');
        return;
      }
    } catch {
      // ignore
    }

    if (typeof IntersectionObserver === 'undefined') {
      node.classList.add('neo-reveal--visible');
      return;
    }

    this.io = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) continue;
          node.classList.add('neo-reveal--visible');
          if (this.revealOnce) this.io?.unobserve(node);
        }
      },
      { threshold: this.revealThreshold, rootMargin: this.revealRootMargin }
    );

    this.io.observe(node);
  }

  ngOnDestroy(): void {
    this.io?.disconnect();
  }
}

