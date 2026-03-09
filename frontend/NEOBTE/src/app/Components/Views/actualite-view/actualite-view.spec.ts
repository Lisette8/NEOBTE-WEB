import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActualiteView } from './actualite-view';

describe('ActualiteView', () => {
  let component: ActualiteView;
  let fixture: ComponentFixture<ActualiteView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActualiteView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActualiteView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
