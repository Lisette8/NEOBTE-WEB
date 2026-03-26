import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountPolicy } from './account-policy';

describe('AccountPolicy', () => {
  let component: AccountPolicy;
  let fixture: ComponentFixture<AccountPolicy>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountPolicy]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountPolicy);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
