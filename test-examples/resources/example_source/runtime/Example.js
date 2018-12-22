
const handler = (req, res) => {
    const hello = {success: true, value: {firstName: 'a', lastName: 'c'}}
    res.send(hello)
}


const handler2 = (req, res) => {
    const hello = {success: true, value: {firstName: 'a', lastName: 'c'}}
    if (req.query.a) {
        res.send(hello)
    } else {
        res.send({them: true, those: false})
    }
}