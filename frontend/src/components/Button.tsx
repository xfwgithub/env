import React from "react";
import "./Button.css";

interface ButtonProps {
  text: string;
  onClick?: () => void;
  variant?: "primary" | "secondary" | "outline";
}

const Button: React.FC<ButtonProps> = ({ text, onClick, variant = "primary" }) => {
  return (
    <button
      className={`button button--${variant}`}
      onClick={onClick}
    >
      {text}
    </button>
  );
};

export default Button;
