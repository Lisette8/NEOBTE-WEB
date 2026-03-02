import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupportView } from './support-view';

describe('SupportView', () => {
  let component: SupportView;
  let fixture: ComponentFixture<SupportView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupportView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupportView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
