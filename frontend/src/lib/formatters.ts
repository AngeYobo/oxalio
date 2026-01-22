/**
 * Formatters & Utilities
 * OXALIO FNE - Frontend
 * 
 * Fonctions utilitaires pour formater les données d'affichage
 */

// ========================================
// Formatage des montants
// ========================================

/**
 * Formater un montant en FCFA
 */
export const formatCurrency = (amount: number, showDecimals: boolean = false): string => {
  const decimals = showDecimals ? 2 : 0;
  
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'XOF',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(amount);
};

/**
 * Formater un montant sans symbole
 */
export const formatNumber = (value: number, decimals: number = 0): string => {
  return new Intl.NumberFormat('fr-FR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
};

/**
 * Formater un pourcentage
 */
export const formatPercent = (value: number, decimals: number = 1): string => {
  return `${formatNumber(value, decimals)}%`;
};

/**
 * Arrondir un montant à 2 décimales
 */
export const roundAmount = (amount: number): number => {
  return Math.round(amount * 100) / 100;
};

// ========================================
// Formatage des dates
// ========================================

/**
 * Formater une date au format français
 */
export const formatDate = (date: string | Date, includeTime: boolean = false): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  
  const options: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  };
  
  if (includeTime) {
    options.hour = '2-digit';
    options.minute = '2-digit';
  }
  
  return new Intl.DateTimeFormat('fr-FR', options).format(d);
};

/**
 * Formater une date de manière courte (ex: "21 Jan 2026")
 */
export const formatDateShort = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  
  return new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(d);
};

/**
 * Formater une date de manière longue (ex: "21 janvier 2026")
 */
export const formatDateLong = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  
  return new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(d);
};

/**
 * Formater une date relative (ex: "il y a 2 heures")
 */
export const formatDateRelative = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffMs = now.getTime() - d.getTime();
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);
  
  if (diffSecs < 60) {
    return 'à l\'instant';
  } else if (diffMins < 60) {
    return `il y a ${diffMins} minute${diffMins > 1 ? 's' : ''}`;
  } else if (diffHours < 24) {
    return `il y a ${diffHours} heure${diffHours > 1 ? 's' : ''}`;
  } else if (diffDays < 7) {
    return `il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;
  } else {
    return formatDateShort(d);
  }
};

/**
 * Obtenir le mois en français
 */
export const getMonthName = (monthIndex: number): string => {
  const months = [
    'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
  ];
  return months[monthIndex];
};

/**
 * Formater une plage de dates
 */
export const formatDateRange = (startDate: string | Date, endDate: string | Date): string => {
  return `${formatDateShort(startDate)} - ${formatDateShort(endDate)}`;
};

// ========================================
// Formatage des références
// ========================================

/**
 * Formater une référence FNE
 */
export const formatFneReference = (reference: string): string => {
  // Format: 2505842N26000000089
  // Découper en groupes pour meilleure lisibilité
  if (!reference) return '';
  
  const ncc = reference.substring(0, 8);      // 2505842N
  const year = reference.substring(8, 10);    // 26
  const number = reference.substring(10);     // 000000089
  
  return `${ncc}-${year}-${number}`;
};

/**
 * Formater un NCC
 */
export const formatNcc = (ncc: string): string => {
  // Format: 2505842N
  if (!ncc) return '';
  return ncc.toUpperCase();
};

// ========================================
// Formatage des téléphones
// ========================================

/**
 * Formater un numéro de téléphone
 */
export const formatPhone = (phone: string): string => {
  if (!phone) return '';
  
  // Supprimer tous les caractères non numériques
  const cleaned = phone.replace(/\D/g, '');
  
  // Format: 07 XX XX XX XX
  if (cleaned.length === 10) {
    return cleaned.replace(/(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1 $2 $3 $4 $5');
  }
  
  return phone;
};

// ========================================
// Formatage des emails
// ========================================

/**
 * Masquer partiellement un email
 */
export const maskEmail = (email: string): string => {
  if (!email) return '';
  
  const [username, domain] = email.split('@');
  if (!domain) return email;
  
  const visibleChars = Math.min(3, Math.floor(username.length / 2));
  const masked = username.substring(0, visibleChars) + '***';
  
  return `${masked}@${domain}`;
};

// ========================================
// Validation
// ========================================

/**
 * Valider un email
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Valider un téléphone ivoirien
 */
export const isValidPhone = (phone: string): boolean => {
  const phoneRegex = /^(0[1-9]|[\+225])[0-9]{8}$/;
  return phoneRegex.test(phone.replace(/\s/g, ''));
};

/**
 * Valider un NCC
 */
export const isValidNcc = (ncc: string): boolean => {
  const nccRegex = /^\d{7}[A-Z]$/;
  return nccRegex.test(ncc);
};

// ========================================
// Formatage de texte
// ========================================

/**
 * Tronquer un texte
 */
export const truncate = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

/**
 * Capitaliser la première lettre
 */
export const capitalize = (text: string): string => {
  if (!text) return '';
  return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
};

/**
 * Convertir en title case
 */
export const toTitleCase = (text: string): string => {
  if (!text) return '';
  return text
    .toLowerCase()
    .split(' ')
    .map(word => capitalize(word))
    .join(' ');
};

/**
 * Générer des initiales
 */
export const getInitials = (name: string): string => {
  if (!name) return '';
  
  const words = name.split(' ').filter(w => w.length > 0);
  if (words.length === 1) {
    return words[0].substring(0, 2).toUpperCase();
  }
  
  return words
    .slice(0, 2)
    .map(w => w[0].toUpperCase())
    .join('');
};

// ========================================
// Couleurs
// ========================================

/**
 * Générer une couleur à partir d'une chaîne
 */
export const stringToColor = (str: string): string => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  const hue = hash % 360;
  return `hsl(${hue}, 70%, 60%)`;
};

// ========================================
// Fichiers
// ========================================

/**
 * Formater une taille de fichier
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * Obtenir l'extension d'un fichier
 */
export const getFileExtension = (filename: string): string => {
  return filename.split('.').pop()?.toLowerCase() || '';
};

// ========================================
// URLs
// ========================================

/**
 * Construire une URL de requête avec des paramètres
 */
export const buildQueryUrl = (baseUrl: string, params: Record<string, any>): string => {
  const searchParams = new URLSearchParams();
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value));
    }
  });
  
  const queryString = searchParams.toString();
  return queryString ? `${baseUrl}?${queryString}` : baseUrl;
};

/**
 * Extraire les paramètres d'une URL
 */
export const getUrlParams = (url: string): Record<string, string> => {
  const params: Record<string, string> = {};
  const urlObj = new URL(url);
  
  urlObj.searchParams.forEach((value, key) => {
    params[key] = value;
  });
  
  return params;
};

// ========================================
// Calculs
// ========================================

/**
 * Calculer un pourcentage
 */
export const calculatePercentage = (value: number, total: number): number => {
  if (total === 0) return 0;
  return roundAmount((value / total) * 100);
};

/**
 * Calculer une variation en pourcentage
 */
export const calculateGrowthRate = (current: number, previous: number): number => {
  if (previous === 0) return current > 0 ? 100 : 0;
  return roundAmount(((current - previous) / previous) * 100);
};

/**
 * Calculer une moyenne
 */
export const calculateAverage = (values: number[]): number => {
  if (values.length === 0) return 0;
  const sum = values.reduce((acc, val) => acc + val, 0);
  return roundAmount(sum / values.length);
};

// ========================================
// Divers
// ========================================

/**
 * Générer un ID aléatoire
 */
export const generateId = (): string => {
  return Math.random().toString(36).substring(2, 9);
};

/**
 * Attendre X millisecondes
 */
export const sleep = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

/**
 * Debounce une fonction
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout>;
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

/**
 * Copier du texte dans le presse-papiers
 */
export const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (error) {
    console.error('Failed to copy to clipboard:', error);
    return false;
  }
};

/**
 * Télécharger un fichier
 */
export const downloadFile = (blob: Blob, filename: string): void => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

export default {
  formatCurrency,
  formatNumber,
  formatPercent,
  formatDate,
  formatDateShort,
  formatDateLong,
  formatDateRelative,
  formatFneReference,
  formatNcc,
  formatPhone,
  isValidEmail,
  isValidPhone,
  isValidNcc,
  truncate,
  capitalize,
  getInitials,
  stringToColor,
  calculatePercentage,
  calculateGrowthRate,
};