import { Box, Button, Dialog, DialogTitle, List, Typography, ListItem } from "@mui/material"
import {useState, useEffect} from "react"

const firstParentId = "27c74670adb75075fad058d5ceaf7b20c4e7786c83bae8a32f626f9782af34c9a33c2046ef60fd2a7878d378e29fec851806bbd9a67878f3a9f1cda4830763fd"

export const Peer = ({ peerId, peersetId, port }) => {

    const [changes, setChanges] = useState([])
    const [open, setOpen] = useState(false)
    const [currentEntryId, setCurrentEntryId] = useState(firstParentId)

    const getPeerBaseAddress = () => `http://172.18.0.2:${port}`

    const askForChanges = () => {
        fetch(`${getPeerBaseAddress()}/v2/change`, {
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then(res => res.json())
            .then(data => setChanges(data))

        fetch(`${getPeerBaseAddress()}/v2/parent_id?peersetId=0`, {
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then(res => res.json())
            .then(data => {
                setCurrentEntryId(data.parentId ?? firstParentId)
            })
    }

    useEffect(() => {
        const interval = setInterval(() => {
            askForChanges()
        }, 1000)

        return () => clearInterval(interval)
    }, [])


    const handleClickOpen = () => setOpen(!open)

    return (
        <Box sx={{width: "100%", height: "20em", backgroundColor: "#d0f0c0", padding: "1em"}}>
            <Typography variant="h5">Peer {peerId}</Typography>
            <CreateChangeButton address={`${getPeerBaseAddress()}/v2/change/async`} changes={changes} currentEntryId={currentEntryId} />

            <Button style={{marginTop: "2em"}} variant="outlined" onClick={handleClickOpen}>View current changes</Button>

            <SimpleDialog open={open}  changes={changes} handleClose={() => setOpen(false)} />
        </Box>
    )
}

const SimpleDialog = ({ open, changes, handleClose }) => {

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Current Changes</DialogTitle>
            <List>
                {changes.map(change => (
                    <SingleChange change={change} key={change?.id ?? "0"} />
                ))}
            </List>
        </Dialog>
    )
}

const SingleChange = ({ change }) => {
    return (
        <ListItem>
            {change.userName}
            {change.id}
        </ListItem>
    )
}

const CreateChangeButton = ({ address, changes, currentEntryId }) => {

    const createChange = () => {
        return {
            "@type": "ADD_USER",
            "peersets": [
              {
                "peersetId": 0,
                "parentId": currentEntryId
             }
            ],
            "userName": `user${changes.length + 1}`
          }
    }

    const introduceChange = () => {
        console.log(`Creating change. Current Entry Id: ${currentEntryId}`)
        fetch(address, {
            method: "POST",
            body: JSON.stringify(createChange()),
            headers: {
                "Content-Type": "application/json"
            }
        })
    }

    return (
        <Button onClick={() => introduceChange()} style={{ marginTop: "2em" }} variant="contained">
            Create Change
        </Button>
    )
}