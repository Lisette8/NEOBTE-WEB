import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CompteManagement } from './compte-management';

describe('CompteManagement', () => {
  let component: CompteManagement;
  let fixture: ComponentFixture<CompteManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompteManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CompteManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
