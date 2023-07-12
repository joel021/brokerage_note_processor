export class User {
    _id: string;
    name: String;
    email: string;
    password: string;
    token: string;
    authorities: Array<string>;
    role: string;
}
