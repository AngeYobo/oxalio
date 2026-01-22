import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';

export default function RegisterSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const [countdown, setCountdown] = useState(10);

  const email = location.state?.email || 'votre email';

  useEffect(() => {
    // Redirection automatique après 10 secondes
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          navigate('/login');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-blue-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full">
        
        {/* Animation de succès */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-24 h-24 bg-green-100 rounded-full mb-6 animate-bounce">
            <svg className="w-16 h-16 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            🎉 Inscription réussie !
          </h1>
          <p className="text-xl text-gray-600">
            Bienvenue sur la plateforme OXALIO
          </p>
        </div>

        {/* Carte principale */}
        <div className="bg-white rounded-lg shadow-xl p-8 mb-6">
          
          {/* Message de confirmation */}
          <div className="mb-8">
            <h2 className="text-2xl font-semibold text-gray-900 mb-4">
              Votre compte a été créé avec succès
            </h2>
            <p className="text-gray-600 mb-4">
              Un email de confirmation a été envoyé à <strong>{email}</strong>
            </p>
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p className="text-blue-800 text-sm">
                📧 Vérifiez votre boîte de réception pour activer votre compte
              </p>
            </div>
          </div>

          {/* Informations sur la période d'essai */}
          <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-6 mb-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-3">
              🎯 Votre période d'essai commence maintenant
            </h3>
            <ul className="space-y-2 text-gray-700">
              <li className="flex items-start">
                <span className="text-green-600 mr-2">✓</span>
                <span><strong>30 jours gratuits</strong> pour tester toutes les fonctionnalités</span>
              </li>
              <li className="flex items-start">
                <span className="text-green-600 mr-2">✓</span>
                <span><strong>50 factures FNE</strong> incluses dans le plan Starter</span>
              </li>
              <li className="flex items-start">
                <span className="text-green-600 mr-2">✓</span>
                <span><strong>Support par email</strong> disponible 24/7</span>
              </li>
              <li className="flex items-start">
                <span className="text-green-600 mr-2">✓</span>
                <span><strong>Aucune carte bancaire</strong> requise pendant l'essai</span>
              </li>
            </ul>
          </div>

          {/* Prochaines étapes */}
          <div className="border-t border-gray-200 pt-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              📋 Prochaines étapes
            </h3>
            <ol className="space-y-3">
              <li className="flex items-start">
                <span className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm mr-3">
                  1
                </span>
                <div>
                  <div className="font-medium text-gray-900">Connectez-vous à votre compte</div>
                  <div className="text-sm text-gray-600">Accédez à votre tableau de bord</div>
                </div>
              </li>
              <li className="flex items-start">
                <span className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm mr-3">
                  2
                </span>
                <div>
                  <div className="font-medium text-gray-900">Configurez votre clé API FNE</div>
                  <div className="text-sm text-gray-600">Obtenez votre clé auprès de la DGI</div>
                </div>
              </li>
              <li className="flex items-start">
                <span className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm mr-3">
                  3
                </span>
                <div>
                  <div className="font-medium text-gray-900">Créez votre première facture</div>
                  <div className="text-sm text-gray-600">Testez la certification FNE</div>
                </div>
              </li>
            </ol>
          </div>

          {/* Bouton de connexion */}
          <div className="mt-8">
            <button
              onClick={() => navigate('/login')}
              className="w-full py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-lg font-semibold hover:from-blue-700 hover:to-indigo-700 transition shadow-lg"
            >
              Se connecter maintenant
            </button>
            <p className="text-center text-sm text-gray-500 mt-3">
              Redirection automatique dans {countdown} secondes...
            </p>
          </div>
        </div>

        {/* Support */}
        <div className="text-center">
          <p className="text-sm text-gray-600">
            Besoin d'aide ?{' '}
            <a href="mailto:support@oxalio.ci" className="text-blue-600 hover:underline font-medium">
              Contactez notre support
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}