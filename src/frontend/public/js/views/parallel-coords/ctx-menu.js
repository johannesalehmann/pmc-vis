import PureContextMenu from "/libs/pure-context-menu/pure-context-menu.js";

const makeCtxMenu = function (divID, pane, fns, { condition, extras }) {

    const items = [
        {
            label: "Sync Selection in Model View",
            preventCloseOnClick: true,
            callback: () => {
                dispatchEvent(new CustomEvent("linked-selection", {
                    detail: {
                        pane: pane.id,
                        selection: fns.getSelection(),
                    },
                }));
            },
        },
        {
            label: "New Pane from Selection...",
            callback: (e) => {
                dispatchEvent(new CustomEvent("pane-from-selection", {
                    detail: {
                        pane: pane.id,
                        selection: fns.getSelection(),
                    },
                }));

                if (pane.cy.pcp) {
                    pane.cy.paneFromPCP(pane);
                }
            },
        },
    ];

    return new PureContextMenu(document.querySelector('#' + divID), items, {
        show: (e, inst) => { 
            if (!e.axisName) {
                inst.setItems(items);
            } else {
                inst.setItems([...items, ...extras]);
            }
            return true
        }
    });
}

export default makeCtxMenu;