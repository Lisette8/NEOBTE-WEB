import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DemandeManagement } from './demande-management';

describe('DemandeManagement', () => {
  let component: DemandeManagement;
  let fixture: ComponentFixture<DemandeManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DemandeManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DemandeManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
