import logo from './logo.svg';
import './App.css';
import { Peer } from './components/Peer';
import { Grid } from '@mui/material';

const peersConf = process.env.REACT_APP_PEERS;

function App() {

  const getPeers = () => {
    const peersPorts = peersConf.split(",")
    const peers = []
    peersPorts.forEach((port, idx) =>
      peers.push(<Grid item xs={3}><Peer peerId={idx} peersetId={0} port={port} /></Grid>)
    )
    return peers
  }

  return (
    <div className="App" style={{padding: "1em"}}>
      <Grid container spacing={10}>
        {getPeers()}
      </Grid>
    </div>
  );
}

export default App;
