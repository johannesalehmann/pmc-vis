const vscode = require('vscode');

const { io } = require("socket.io-client");
const constants = require("./constants.js");

class Communication {

    constructor() {
        const address = `http://${constants.ADDRESS}:8082`
        this._socket = io(address);

        console.log("opened socket on " + address)

        this._socket.on("connect", () => {
            vscode.window.showWarningMessage("Socket Connected");
        })

        this._socket.on("disconnect", (reason, details) => {
            vscode.window.showWarningMessage("Socket Disconnected\n Reason: " + reason);
        })

        this._socket.on("disconnect", (reason, details) => {
            vscode.window.showWarningMessage("Socket Disconnected\n Reason: " + reason);
        })

        this._socket.on("connect_error", () => {
            vscode.window.showWarningMessage("Socket Connection Failed");
        });

        this._socket.on(constants.EVENT_STATUS, (data) => {
            console.log(data)
        })

        this._socket.on("MESSAGE", (data) => {
            vscode.window.showInformationMessage("Someone says: " + data);
        })
    }

    send(event, data) {
        this._socket.emit(event, data)
    }

    register(event, handler) {
        this._socket.on(event, handler);
    }
}

module.exports = { Communication }