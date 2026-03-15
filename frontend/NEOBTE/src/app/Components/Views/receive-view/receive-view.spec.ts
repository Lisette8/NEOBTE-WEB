import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceiveView } from './receive-view';

describe('ReceiveView', () => {
  let component: ReceiveView;
  let fixture: ComponentFixture<ReceiveView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReceiveView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReceiveView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
