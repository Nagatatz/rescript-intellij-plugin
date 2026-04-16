// Wire format for the /api/users endpoints. Keep server and client
// in sync by editing just this file.
type createUserReq = {name: string, email: string}
type createUserRes = {id: int, name: string, email: string}
