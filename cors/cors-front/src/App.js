import logo from './logo.svg';
import './App.css';
import axios from 'axios';
import { useState } from 'react';

function App() {
    const [getText, setGetText] = useState("get");
    const [putText, setPutText] = useState("put");
    const [postText, setPostText] = useState("post");
    const [deleteText, setDeleteText] = useState("delete");

    // header에 아무것도 안넣으면 기본적으로 x-www-form-urlencoded로 들어가므로 preflight가 안들어간다.
    const onGetClick = async () => {
        let response = await axios.get("http://localhost/monitor/hello",
            {
                headers: {
                    'Content-Type': 'application/json'
                }
            }
            );
        console.log(response.data)
        setGetText(() => response.data)
    }

    const onPostClick = async () => {
        let response = await axios.post("http://localhost/monitor/hello",null,
            {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        console.log(response.data)
        setPostText(() => response.data)
    }

    const onPutClick = async () => {
        let response = await axios.put("http://localhost/monitor/hello");
        console.log(response.data)
        setPutText(() => response.data)
    }

    const onDeleteClick = async () => {
        let response = await axios.delete("http://localhost/monitor/hello");
        console.log(response.data)
        setDeleteText(() => response.data)
    }

    return (
        <div className="App">
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo" />
                <button style={{ height: 100, width: 100 }} onClick={onGetClick}>{getText}</button>
                <button style={{ height: 100, width: 100 }} onClick={onPutClick}>{putText}</button>
                <button style={{ height: 100, width: 100 }} onClick={onPostClick}>{postText}</button>
                <button style={{ height: 100, width: 100 }} onClick={onDeleteClick}>{deleteText}</button>
            </header>
        </div>
    );
}

export default App;
