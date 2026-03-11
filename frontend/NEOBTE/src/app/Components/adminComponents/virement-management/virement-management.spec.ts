import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VirementManagement } from './virement-management';

describe('VirementManagement', () => {
  let component: VirementManagement;
  let fixture: ComponentFixture<VirementManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VirementManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VirementManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
