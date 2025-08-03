'use client';

import { useState, useEffect } from 'react';

// Describes the structure of a single certificate object
interface Certificate {
  commonName: string;
  issuer: string;
  validFrom: string;
  validTo: string;
  version: number;
  serialNumber: string;
  signatureAlgorithm: string;
  publicKey: string;
  // Add other fields if the API provides them
}

const ITEMS_PER_PAGE = 10;

export default function CertificatesPage() {
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCertificate, setSelectedCertificate] = useState<Certificate | null>(null);
  const [checkedCertificates, setCheckedCertificates] = useState<string[]>([]);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isConfirmModalOpen, setConfirmModalOpen] = useState<boolean>(false);

  const fetchCertificates = async () => {
    try {
      const response = await fetch('http://localhost:18080/certificates');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setCertificates(data);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCertificates(); // Initial fetch
    const intervalId = setInterval(fetchCertificates, 10000); // Poll every 10 seconds
    return () => clearInterval(intervalId); // Cleanup on component unmount
  }, []);

  // --- Pagination Logic ---
  const totalPages = Math.ceil(certificates.length / ITEMS_PER_PAGE);
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const currentCertificates = certificates.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  const handlePageChange = (newPage: number) => {
    if (newPage > 0 && newPage <= totalPages) {
      setCurrentPage(newPage);
      setSelectedCertificate(null); // Deselect on page change
    }
  };

  // --- Selection and Deletion Logic ---
  const handleRowClick = (cert: Certificate) => {
    if (selectedCertificate && selectedCertificate.commonName === cert.commonName) {
      setSelectedCertificate(null); // Toggle off if the same row is clicked
    } else {
      setSelectedCertificate(cert);
    }
  };

  const handleCheckboxChange = (commonName: string) => {
    setCheckedCertificates(prev =>
      prev.includes(commonName)
        ? prev.filter(cn => cn !== commonName)
        : [...prev, commonName]
    );
  };

  const handleSelectAllChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      const allOnPage = currentCertificates.map(c => c.commonName);
      setCheckedCertificates(prev => [...new Set([...prev, ...allOnPage])]);
    } else {
      const allOnPage = currentCertificates.map(c => c.commonName);
      setCheckedCertificates(prev => prev.filter(cn => !allOnPage.includes(cn)));
    }
  };

  const handleDeleteClick = () => {
    if (checkedCertificates.length > 0) {
      setConfirmModalOpen(true);
    }
  };

  const handleConfirmDelete = async () => {
    const promises = checkedCertificates.map(cn =>
      fetch(`http://localhost:18080/certificates/${cn}`, { method: 'DELETE' })
    );

    const results = await Promise.allSettled(promises);

    const successfullyDeleted: string[] = [];
    results.forEach((result, index) => {
      if (result.status === 'fulfilled' && result.value.ok) {
        successfullyDeleted.push(checkedCertificates[index]);
      }
    });

    // Refresh and clean up
    setConfirmModalOpen(false);
    setCheckedCertificates(prev => prev.filter(cn => !successfullyDeleted.includes(cn)));
    fetchCertificates(); // Re-fetch to get the latest list
  };

  const areAllOnPageSelected = currentCertificates.length > 0 && currentCertificates.every(c => checkedCertificates.includes(c.commonName));
  const areSomeOnPageSelected = currentCertificates.some(c => checkedCertificates.includes(c.commonName));

  // --- Render Logic ---
  if (loading) {
    return <main className="flex min-h-screen flex-col items-center justify-center p-4"><p>Loading certificates...</p></main>;
  }

  if (error) {
    return <main className="flex min-h-screen flex-col items-center justify-center p-4"><p className="text-red-500">Error: {error}</p></main>;
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* Control Area */}
      <div className="flex-shrink-0 bg-white border-b border-gray-200 p-2">
        <div className="flex items-center space-x-2">
          <button className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500">
            New
          </button>
          <button
            onClick={handleDeleteClick}
            className="px-4 py-2 text-sm font-medium text-white bg-gray-500 rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 disabled:bg-gray-300 disabled:cursor-not-allowed"
            disabled={checkedCertificates.length === 0}
          >
            Delete
          </button>
        </div>
      </div>

      {/* Status Area */}
      <div className="flex flex-grow overflow-hidden">
        {/* Table Area */}
        <div className="flex-grow p-4 overflow-auto">
          <div className="bg-white shadow rounded-lg">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="w-12 px-4 py-3">
                    <input
                      type="checkbox"
                      className="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                      onChange={handleSelectAllChange}
                      checked={areAllOnPageSelected}
                      ref={input => {
                        if (input) {
                          input.indeterminate = areSomeOnPageSelected && !areAllOnPageSelected;
                        }
                      }}
                    />
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Common Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valid From</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valid To</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {currentCertificates.map((cert) => (
                  <tr
                    key={cert.commonName}
                    onClick={() => handleRowClick(cert)}
                    className={`cursor-pointer ${selectedCertificate?.commonName === cert.commonName ? 'bg-blue-100' : 'hover:bg-gray-50'}`}
                  >
                    <td className="px-4 py-4 whitespace-nowrap">
                      <input
                        type="checkbox"
                        className="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                        checked={checkedCertificates.includes(cert.commonName)}
                        onChange={(e) => {
                          e.stopPropagation(); // Prevent row click event
                          handleCheckboxChange(cert.commonName);
                        }}
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{cert.commonName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(cert.validFrom).toLocaleDateString()}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(cert.validTo).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center mt-4 space-x-2">
              <button onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1} className="px-3 py-1 border rounded-md disabled:opacity-50">
                Previous
              </button>
              <span>Page {currentPage} of {totalPages}</span>
              <button onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages} className="px-3 py-1 border rounded-md disabled:opacity-50">
                Next
              </button>
            </div>
          )}
        </div>

        {/* Details Area (Conditional) */}
        {selectedCertificate && (
          <div className="relative w-1/3 flex-shrink-0 border-l border-gray-200 bg-white p-4 overflow-auto">
            <button
              onClick={() => setSelectedCertificate(null)}
              className="absolute top-3 right-3 text-gray-400 hover:text-gray-600"
              aria-label="Close"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <h3 className="text-lg font-bold mb-4">Certificate Details</h3>
            <div className="space-y-2 text-sm">
              <div><strong>Common Name:</strong> {selectedCertificate.commonName}</div>
              <div><strong>Issuer:</strong> {selectedCertificate.issuer}</div>
              <div><strong>Valid From:</strong> {new Date(selectedCertificate.validFrom).toLocaleString()}</div>
              <div><strong>Valid To:</strong> {new Date(selectedCertificate.validTo).toLocaleString()}</div>
              <div><strong>Serial Number:</strong> {selectedCertificate.serialNumber}</div>
              <div><strong>Version:</strong> {selectedCertificate.version}</div>
              <div className="break-all"><strong>Signature Algorithm:</strong> {selectedCertificate.signatureAlgorithm}</div>
              <div className="mt-4">
                <strong className="block mb-1">Public Key:</strong>
                <textarea readOnly className="w-full h-48 p-2 border rounded-md font-mono text-xs bg-gray-100" value={selectedCertificate.publicKey}></textarea>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Deletion Confirmation Modal */}
      {isConfirmModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded-lg shadow-xl">
            <h3 className="text-lg font-bold mb-4">Confirm Deletion</h3>
            <p className="mb-4">Are you sure you want to delete the following certificates?</p>
            <ul className="list-disc list-inside mb-4 max-h-40 overflow-y-auto">
              {checkedCertificates.map(cn => <li key={cn}>{cn}</li>)}
            </ul>
            <div className="flex justify-end space-x-2">
              <button
                onClick={() => setConfirmModalOpen(false)}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
