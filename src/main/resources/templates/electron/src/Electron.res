// Bindings over the `window.electronAPI` object exposed by preload.cjs.
type info = {name: string, electronVersion: string, platform: string, arch: string}

@val external electronAPI: {"getInfo": unit => promise<info>} = "electronAPI"

let getInfo = (): promise<info> => electronAPI["getInfo"]()
