import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Link from "next/link";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Jwt License Server",
  description: "Frontend for Jwt License Server",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <header className="bg-gray-800 text-white p-4">
          <h1 className="text-2xl font-bold">Jwt License Server</h1>
        </header>
        <nav className="bg-gray-700 p-4">
          <ul className="flex space-x-4">
            <li>
              <Link href="/certificates" className="text-white hover:text-gray-300">
                Certificate
              </Link>
            </li>
            <li>
              <Link href="/license" className="text-white hover:text-gray-300">
                License
              </Link>
            </li>
            <li>
              <Link href="/history" className="text-white hover:text-gray-300">
                History
              </Link>
            </li>
            <li>
              <Link href="/schema" className="text-white hover:text-gray-300">
                Schema
              </Link>
            </li>
          </ul>
        </nav>
        <div className="p-4">
          {children}
        </div>
      </body>
    </html>
  );
}
