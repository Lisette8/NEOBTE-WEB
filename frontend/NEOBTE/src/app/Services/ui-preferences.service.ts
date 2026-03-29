import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

type LocalStorageKey = 'neo.notifSoundEnabled';

@Injectable({ providedIn: 'root' })
export class UiPreferencesService {
  private readonly SOUND_KEY: LocalStorageKey = 'neo.notifSoundEnabled';

  private notifSoundEnabledSubject = new BehaviorSubject<boolean>(this.readBoolean(this.SOUND_KEY, true));
  readonly notifSoundEnabled$ = this.notifSoundEnabledSubject.asObservable();

  private audioCtx: AudioContext | null = null;

  get notifSoundEnabled(): boolean {
    return this.notifSoundEnabledSubject.value;
  }

  setNotifSoundEnabled(enabled: boolean) {
    this.notifSoundEnabledSubject.next(enabled);
    try {
      localStorage.setItem(this.SOUND_KEY, enabled ? '1' : '0');
    } catch {
      // ignore
    }
  }

  /** Call from a user gesture (pointerdown/click) to reduce autoplay-blocked sounds. */
  unlockAudio() {
    try {
      if (this.audioCtx) {
        const ctx = this.audioCtx;
        if (ctx.state === 'suspended') void ctx.resume();
        return;
      }
      const AudioCtx = (window as any).AudioContext || (window as any).webkitAudioContext;
      if (!AudioCtx) return;
      this.audioCtx = new AudioCtx();
      const ctx = this.audioCtx;
      if (ctx && ctx.state === 'suspended') void ctx.resume();
    } catch {
      // ignore
    }
  }

  playNotificationSound() {
    if (!this.notifSoundEnabled) return;
    try {
      const ctx = this.getOrCreateAudioCtx();
      if (!ctx) return;

      const o = ctx.createOscillator();
      const g = ctx.createGain();
      o.type = 'sine';
      o.frequency.value = 880;
      g.gain.setValueAtTime(0.0001, ctx.currentTime);
      g.gain.exponentialRampToValueAtTime(0.055, ctx.currentTime + 0.01);
      g.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.11);
      o.connect(g);
      g.connect(ctx.destination);
      o.start();
      o.stop(ctx.currentTime + 0.12);
    } catch {
      // ignore (autoplay policies / unsupported env)
    }
  }

  private getOrCreateAudioCtx(): AudioContext | null {
    try {
      if (!this.audioCtx) {
        const AudioCtx = (window as any).AudioContext || (window as any).webkitAudioContext;
        if (!AudioCtx) return null;
        this.audioCtx = new AudioCtx();
      }
      const ctx = this.audioCtx;
      if (!ctx) return null;
      if (ctx.state === 'suspended') void ctx.resume();
      return ctx;
    } catch {
      return null;
    }
  }

  private readBoolean(key: LocalStorageKey, fallback: boolean): boolean {
    try {
      const v = localStorage.getItem(key);
      if (v === null) return fallback;
      return v === '1' || v === 'true';
    } catch {
      return fallback;
    }
  }
}
