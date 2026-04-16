// Request/response types shared by server and client.
type createUserReq = {name: string, email: string}
type createUserRes = {id: int, name: string, email: string}
