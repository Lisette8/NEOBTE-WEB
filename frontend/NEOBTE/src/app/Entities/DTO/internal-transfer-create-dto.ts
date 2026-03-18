export interface InternalTransferCreateDTO {
  compteSourceId: number;
  compteDestinationId: number;
  montant: number;
  idempotencyKey: string;
}

