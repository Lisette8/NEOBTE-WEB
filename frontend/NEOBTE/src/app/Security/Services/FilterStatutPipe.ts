import { Pipe, PipeTransform } from "@angular/core";
import { FraudeAlerte, FraudeStatut } from "../../Entities/Interfaces/fraude";

@Pipe({ name: 'filterStatut', standalone: true, pure: false })
export class FilterStatutPipe implements PipeTransform {
  transform(alertes: FraudeAlerte[], statut: FraudeStatut): number {
    return alertes.filter(a => a.statut === statut).length;
  }
}