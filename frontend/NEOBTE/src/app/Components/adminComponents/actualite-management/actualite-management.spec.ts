import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActualiteManagement } from './actualite-management';

describe('ActualiteManagement', () => {
  let component: ActualiteManagement;
  let fixture: ComponentFixture<ActualiteManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActualiteManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActualiteManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
