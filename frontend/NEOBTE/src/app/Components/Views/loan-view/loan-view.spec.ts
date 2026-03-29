import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanView } from './loan-view';

describe('LoanView', () => {
  let component: LoanView;
  let fixture: ComponentFixture<LoanView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoanView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
