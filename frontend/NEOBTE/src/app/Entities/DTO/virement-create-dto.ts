export interface VirementCreateDTO {
  compteSourceId: number;
  compteDestinationId: number;
  montant: number;
  idempotencyKey: string;
}