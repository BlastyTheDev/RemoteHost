import type {Metadata} from "next";
import {Inter, Lexend_Exa} from "next/font/google";
import "./globals.css";
import React from "react";

// const inter = Inter({subsets: ["latin"]});
const lexendExa = Lexend_Exa({subsets: ["latin"]});

export const metadata: Metadata = {
    title: "Create Next App",
    description: "Generated by create next app",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
        <body className={`${lexendExa.className} antialiased`}>{children}</body>
        </html>
    );
}
