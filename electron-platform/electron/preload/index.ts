import { contextBridge } from 'electron'

contextBridge.exposeInMainWorld('platformDesktop', {
  version: '0.1.0'
})
