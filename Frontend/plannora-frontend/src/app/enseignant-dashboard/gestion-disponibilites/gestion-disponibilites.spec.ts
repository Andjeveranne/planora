import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionDisponibilites } from './gestion-disponibilites';

describe('GestionDisponibilites', () => {
  let component: GestionDisponibilites;
  let fixture: ComponentFixture<GestionDisponibilites>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionDisponibilites]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionDisponibilites);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
