import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvestmentManagement } from './investment-management';

describe('InvestmentManagement', () => {
  let component: InvestmentManagement;
  let fixture: ComponentFixture<InvestmentManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmentManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvestmentManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
