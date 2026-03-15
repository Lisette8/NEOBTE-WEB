export interface VirementCreateDTO {
  recipientIdentifier: string;
  montant: number;
  idempotencyKey: string;
}