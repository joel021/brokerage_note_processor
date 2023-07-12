import * as uuid from 'uuid';

export class Operation {

    operationId: Number

    userId:uuid;

    name:string;

    type:string;

    qtd:number;

    value:number;

    date: Date;

    typeOp: string;

    typeMarket:string;
    fileId:uuid;

    wallet: string;

    closeMonth:string;
    deletedAt:Date;
}