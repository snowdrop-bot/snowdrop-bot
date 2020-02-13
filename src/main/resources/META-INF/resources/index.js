var closeables = []

function cleanUp() {
    closeables.forEach(function(value) {
        value.close()
    })
}

function registerClosable(c) {
    closeables.push(c)
}
