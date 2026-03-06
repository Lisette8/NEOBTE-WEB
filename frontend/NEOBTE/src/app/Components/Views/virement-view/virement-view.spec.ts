import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VirementView } from './virement-view';

describe('VirementView', () => {
  let component: VirementView;
  let fixture: ComponentFixture<VirementView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VirementView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VirementView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
