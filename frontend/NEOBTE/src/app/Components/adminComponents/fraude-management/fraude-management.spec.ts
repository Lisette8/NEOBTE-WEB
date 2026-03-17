import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FraudeManagement } from './fraude-management';

describe('FraudeManagement', () => {
  let component: FraudeManagement;
  let fixture: ComponentFixture<FraudeManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FraudeManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FraudeManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
