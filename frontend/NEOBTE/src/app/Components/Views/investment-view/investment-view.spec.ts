import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvestmentView } from './investment-view';

describe('InvestmentView', () => {
  let component: InvestmentView;
  let fixture: ComponentFixture<InvestmentView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmentView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvestmentView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
