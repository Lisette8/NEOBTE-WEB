import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CompteView } from './compte-view';

describe('CompteView', () => {
  let component: CompteView;
  let fixture: ComponentFixture<CompteView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompteView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CompteView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
