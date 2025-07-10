import { logout} from "./utils/auth.js";

function App() {


    return (
        <div>
            <h1>THE APP</h1>
            <button onClick={logout}>Logout</button>
        </div>
    );
}
export default App;