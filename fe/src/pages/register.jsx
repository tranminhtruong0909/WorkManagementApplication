import React, { useState } from 'react';
import { register } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Register = () =>{
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [avatar, setAvatar] = useState(null);
    const [name, setName] = useState('');
    const [roale, setRole] = useState('');

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
          await register({ email, password, confirmPassword, avatar, name, roale });
          // Đăng ký thành công, chuyển hướng đến trang đăng nhập
          navigate('/login');
        } catch (error) {
          console.error('Register failed:', error);
        }

    };
    return(
        <div>
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="bg-white p-6 rounded shadow-lg w-96">
                    <h2 className="text-2xl font-bold mb-4">Register</h2>
                    <form className='bg-white p-6 rounded shadow-lg' >
                        
                    </form>
                </div>
               
            </div>
        </div>
    );

}