import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountDetailView } from './account-detail-view';

describe('AccountDetailView', () => {
  let component: AccountDetailView;
  let fixture: ComponentFixture<AccountDetailView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountDetailView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountDetailView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
