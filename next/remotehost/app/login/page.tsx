export default function Login() {
    return (
        <div className="bg-panel-color w-[20.688rem] h-[17.125rem] m-auto rounded-xl text-text-color tracking-[-0.075rem]">
            <input type="text" placeholder="username"
                   className="m-[2.0625rem] mb-0 w-[16.563rem] h-[2.5rem] bg-panel-color border border-b-text-color
                   rounded-lg p-2.5 text-base" />
            <input type="password" placeholder="password"
                   className="m-[2.0625rem] mt-4 mb-0 w-[16.563rem] h-[2.5rem] bg-panel-color border border-b-text-color
                   rounded-lg p-2.5 text-base" />
            <div className="text-white">
                <input type="checkbox" name="remember" />
                <label htmlFor="remember">remember me</label>
            </div>
            <button
                className="m-[2.0625rem] mt-4 w-[16.563rem] h-[2.5rem] bg-text-color text-[#000000] rounded-lg">
                Login
            </button>
        </div>
    );
};