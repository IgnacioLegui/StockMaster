import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  Download,
  Github,
  ShieldCheck,
  Zap,
  WifiOff,
  BarChart3,
  BellRing,
  FileOutput,
  Search,
  CheckCircle2,
  Monitor,
  Globe,
  Linkedin,
  AlertTriangle,
  ShoppingCart,
  ClipboardList,
  Languages,
  TrendingUp
} from 'lucide-react';

// Animations
const fadeInUp = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } }
};

const staggerContainer = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.2
    }
  }
};

const scrollReveal = {
  hidden: { opacity: 0, y: 50 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.8, ease: "easeOut" }
  }
};

const translations = {
  es: {
    nav: {
      features: "Características",
      why: "Por qué Desktop",
      start: "Empezar",
      download: "Descargar v4.0"
    },
    hero: {
      avail: "Disponible para Windows 10/11",
      titlePre: "Control de Inventario",
      titleSpan: "Profesional",
      titlePost: ", Directo en tu Escritorio.",
      desc: "Olvídate de las hojas de cálculo. Gestiona stock, ventas POS, órdenes de compra, reportes y más con una interfaz profesional bilingüe. Gratis, potente y",
      private: "100% privado",
      btnDownload: "Descargar Gratis",
      btnCode: "Ver Código"
    },
    status: {
      label: "Estado del Sistema",
      value: "Optimizado"
    },
    why: {
      title: "¿Por qué una App de Escritorio?",
      subtitle: "En un mundo lleno de suscripciones web, volvemos a lo básico: rendimiento puro y propiedad real.",
      privacyTitle: "Privacidad Total",
      privacyDesc: "Tus datos comerciales son tuyos. Se almacenan localmente en SQLite, lejos de servidores de terceros.",
      speedTitle: "Velocidad Nativa",
      speedDesc: "Sin tiempos de carga del navegador. Disfruta de la fluidez y potencia de JavaFX corriendo directamente en tu hardware.",
      offlineTitle: "Sin Conexión",
      offlineDesc: "Trabaja donde quieras. Tu inventario está disponible 24/7, incluso si se cae internet."
    },
    features: {
      title: "Todo lo que necesitas para crecer",
      subtitle: "Herramientas profesionales simplificadas.",
      dashboardTitle: "Panel de Control Visual",
      dashboardDesc: "Métricas clave en tiempo real: valor del inventario, productos totales y alertas críticas en una sola vista.",
      alertsTitle: "Alertas Inteligentes",
      alertsDesc: "Notificaciones automáticas cuando el stock baja del mínimo.",
      exportTitle: "Exportación PDF y Excel",
      exportDesc: "Genera reportes de inventario, ventas y márgenes con un solo clic.",
      searchTitle: "Búsqueda Rápida",
      searchDesc: "Filtra por nombre, categoría o código de barras.",
      posTitle: "Punto de Venta (POS)",
      posDesc: "Carrito de compras, cálculo automático de IVA, múltiples métodos de pago, escaneo de códigos de barras e impresión de recibos.",
      poTitle: "Órdenes de Compra",
      poDesc: "Crea, recibe y gestiona órdenes de compra. El stock se actualiza automáticamente al recibir.",
      reportsTitle: "Reportes Avanzados",
      reportsDesc: "Resumen de ventas, productos top, movimientos de stock, márgenes y lotes por vencer.",
      i18nTitle: "Bilingüe ES/EN",
      i18nDesc: "Interfaz completa en español e inglés con cambio instantáneo."
    },
    steps: {
      title: "Empieza en 3 Pasos",
      subtitle: "Sin configuraciones complicadas. Instalar y usar.",
      s1Title: "Descarga el Instalador",
      s1Desc: "Obtén la última versión del archivo .exe desde GitHub o descarga directa aquí.",
      s2Title: "Ejecución Instantánea",
      s2Desc: "Sigue el asistente de instalación de Windows. En segundos estarás listo.",
      s3Title: "Toma el Control",
      s3Desc: "Añade tus primeros productos y proveedores. ¡Tu inventario organizado hoy mismo!"
    },
    footer: {
      desc: "Software de código abierto diseñado para empoderar a pequeños negocios con herramientas de nivel empresarial.",
      createdBy: "Creado por",
      role: "Ignacio Leguizamon",
      quote: "\"Diseñado por un desarrollador, para dueños de negocios que valoran su tiempo.\"",
      openSource: "2026 StockMaster. Open Source (MIT License).",
      star: "Si te gusta este proyecto, dale una"
    }
  },
  en: {
    nav: {
      features: "Features",
      why: "Why Desktop",
      start: "Get Started",
      download: "Download v4.0"
    },
    hero: {
      avail: "Available for Windows 10/11",
      titlePre: "Professional Inventory",
      titleSpan: "Control",
      titlePost: ", Right on your Desktop.",
      desc: "Forget spreadsheets. Manage stock, POS sales, purchase orders, reports and more with a professional bilingual interface. Free, powerful, and",
      private: "100% private",
      btnDownload: "Download Free",
      btnCode: "View Code"
    },
    status: {
      label: "System Status",
      value: "Optimized"
    },
    why: {
      title: "Why a Desktop App?",
      subtitle: "In a world full of web subscriptions, we go back to basics: pure performance and real ownership.",
      privacyTitle: "Total Privacy",
      privacyDesc: "Your business data is yours. Stored locally in SQLite, far from third-party servers.",
      speedTitle: "Native Speed",
      speedDesc: "No browser load times. Enjoy the fluidity and power of JavaFX running directly on your hardware.",
      offlineTitle: "Offline Ready",
      offlineDesc: "Work wherever you want. Your inventory is available 24/7, even if the internet goes down."
    },
    features: {
      title: "Everything you need to grow",
      subtitle: "Professional tools simplified.",
      dashboardTitle: "Visual Dashboard",
      dashboardDesc: "Key metrics in real-time: inventory value, total products, and critical alerts in a single view.",
      alertsTitle: "Smart Alerts",
      alertsDesc: "Automatic notifications when stock drops below minimum.",
      exportTitle: "PDF & Excel Export",
      exportDesc: "Generate inventory, sales, and margin reports with a single click.",
      searchTitle: "Quick Search",
      searchDesc: "Filter by name, category, or barcode.",
      posTitle: "Point of Sale (POS)",
      posDesc: "Shopping cart, automatic tax calculation, multiple payment methods, barcode scanning, and receipt printing.",
      poTitle: "Purchase Orders",
      poDesc: "Create, receive, and manage purchase orders. Stock updates automatically on receipt.",
      reportsTitle: "Advanced Reports",
      reportsDesc: "Sales summary, top products, stock movements, profit margins, and expiring batches.",
      i18nTitle: "Bilingual ES/EN",
      i18nDesc: "Full interface in Spanish and English with instant switching."
    },
    steps: {
      title: "Start in 3 Steps",
      subtitle: "No complicated setups. Install and use.",
      s1Title: "Download Installer",
      s1Desc: "Get the latest .exe version from GitHub or direct download here.",
      s2Title: "Instant Execution",
      s2Desc: "Follow the Windows installation wizard. You'll be ready in seconds.",
      s3Title: "Take Control",
      s3Desc: "Add your first products and suppliers. Your inventory organized today!"
    },
    footer: {
      desc: "Open source software designed to empower small businesses with enterprise-grade tools.",
      createdBy: "Created by",
      role: "Ignacio Leguizamon",
      quote: "\"Designed by a developer, for business owners who value their time.\"",
      openSource: "2026 StockMaster. Open Source (MIT License).",
      star: "If you like this project, give it a"
    }
  }
};

function App() {
  const [lang, setLang] = useState('es');

  // Hardcoded Dark Mode Force
  useEffect(() => {
    document.documentElement.classList.add('dark');
  }, []);

  const text = translations[lang];

  const toggleLang = () => {
    setLang(prev => prev === 'es' ? 'en' : 'es');
  };

  const scrollToSection = (e, id) => {
    e.preventDefault();
    const element = document.getElementById(id);
    if (element) {
      // Offset for fixed header
      const headerOffset = 80;
      const elementPosition = element.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

      window.scrollTo({
        top: offsetPosition,
        behavior: "smooth"
      });
    }
  };

  return (
    <div className="min-h-screen bg-background text-text overflow-x-hidden selection:bg-primary selection:text-white font-sans">

      {/* Navbar */}
      <nav className="fixed w-full z-50 bg-background/80 backdrop-blur-md border-b border-white/10 transition-colors duration-300">
        <div className="container mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="bg-primary p-2 rounded-lg">
              <BarChart3 className="w-6 h-6 text-white" />
            </div>
            <span className="text-xl font-bold tracking-tight text-white">StockMaster</span>
          </div>

          <div className="hidden md:flex items-center gap-8 text-sm font-medium text-textMuted">
            <a href="#features" onClick={(e) => scrollToSection(e, 'features')} className="hover:text-white transition-colors cursor-pointer">{text.nav.features}</a>
            <a href="#why-desktop" onClick={(e) => scrollToSection(e, 'why-desktop')} className="hover:text-white transition-colors cursor-pointer">{text.nav.why}</a>
            <a href="#start" onClick={(e) => scrollToSection(e, 'start')} className="hover:text-white transition-colors cursor-pointer">{text.nav.start}</a>
          </div>

          <div className="flex items-center gap-4">
            {/* Language Toggle */}
            <button
              onClick={toggleLang}
              className="flex items-center gap-1 text-sm font-medium p-2 rounded-full hover:bg-white/10 transition-colors text-textMuted hover:text-white"
              aria-label="Toggle Language"
            >
              <Globe className="w-4 h-4" />
              <span>{lang.toUpperCase()}</span>
            </button>

            <a href="/StockMaster_Setup.exe" className="bg-primary hover:bg-blue-600 text-white px-5 py-2 rounded-full text-sm font-medium transition-all shadow-lg shadow-blue-500/20 shadow-primary/20">
              {text.nav.download}
            </a>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative pt-24 pb-16 lg:pt-32 lg:pb-24 overflow-hidden">
        {/* Abstract Background Elements */}
        <div className="absolute top-20 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-primary/20 rounded-full blur-[120px] -z-10 animate-pulse-slow" />
        <div className="absolute bottom-0 right-0 w-[400px] h-[400px] bg-secondary/10 rounded-full blur-[100px] -z-10" />

        <div className="container mx-auto px-6 text-center z-10 relative">
          <motion.div
            initial="hidden"
            animate="visible"
            variants={staggerContainer}
            className="max-w-4xl mx-auto"
          >
            <motion.div variants={fadeInUp} className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 mb-6 backdrop-blur-sm">
              <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
              <span className="text-sm font-medium text-textMuted">{text.hero.avail}</span>
            </motion.div>

            <motion.h1 variants={fadeInUp} className="text-5xl lg:text-7xl font-bold tracking-tight mb-6 leading-tight text-white text-shadow">
              {text.hero.titlePre} <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-blue-400">{text.hero.titleSpan}</span>
              {text.hero.titlePost}
            </motion.h1>

            <motion.p variants={fadeInUp} className="text-xl text-textMuted mb-8 max-w-2xl mx-auto leading-relaxed">
              {text.hero.desc} <span className="text-white font-semibold">{text.hero.private}</span>.
            </motion.p>

            <motion.div variants={fadeInUp} className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
              <a href="/StockMaster_Setup.exe" className="group relative px-8 py-4 bg-primary hover:bg-blue-600 text-white rounded-xl font-semibold text-lg transition-all shadow-lg shadow-primary/25 hover:shadow-primary/40 flex items-center gap-3 w-full sm:w-auto justify-center">
                <Download className="w-5 h-5 group-hover:-translate-y-1 transition-transform" />
                {text.hero.btnDownload}
              </a>
              <a href="https://github.com/IgnacioLegui/StockMaster" target="_blank" rel="noopener noreferrer" className="group px-8 py-4 bg-surface hover:bg-surface/80 text-white border border-white/10 rounded-xl font-medium text-lg transition-all flex items-center gap-3 w-full sm:w-auto justify-center">
                <Github className="w-5 h-5 text-textMuted group-hover:text-white transition-colors" />
                {text.hero.btnCode}
              </a>
            </motion.div>

            {/* SmartScreen Warning Note */}
            <motion.div variants={fadeInUp} className="text-sm text-amber-400/80 bg-amber-900/10 border border-amber-900/20 rounded-lg p-3 max-w-lg mx-auto backdrop-blur-sm mb-12">
              <div className="flex items-start gap-2 text-left">
                <AlertTriangle className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold mb-1">Windows Protected Your PC?</p>
                  <p className="text-textMuted/80 text-xs leading-relaxed">
                    This is a known false positive for new open-source apps. Click <span className="font-bold text-white">More info</span> &rarr; <span className="font-bold text-white">Run anyway</span> to install.
                  </p>
                </div>
              </div>
            </motion.div>
          </motion.div>

          {/* App Mockup */}
          <motion.div
            initial={{ opacity: 0, y: 100, rotateX: 20 }}
            animate={{ opacity: 1, y: 0, rotateX: 0 }}
            transition={{ duration: 1, delay: 0.4 }}
            className="relative mx-auto max-w-5xl perspective-1000"
          >
            <div className="relative rounded-xl border border-white/10 bg-surface/50 backdrop-blur-xl shadow-2xl overflow-hidden p-2">
              <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-b from-white/5 to-transparent pointer-events-none" />
              <img
                src="/screenshot.png"
                alt="StockMaster Dashboard Screenshot"
                className="rounded-lg w-full shadow-2xl opacity-90 object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-transparent h-40 bottom-0 top-auto" />
            </div>

            {/* Floating Badge */}
            <motion.div
              animate={{ y: [0, -20, 0] }}
              transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
              className="absolute -right-8 top-20 bg-surface border border-white/10 p-4 rounded-2xl shadow-xl hidden lg:block"
            >
              <div className="flex items-center gap-3">
                <div className="bg-green-500/20 p-2 rounded-lg">
                  <CheckCircle2 className="w-6 h-6 text-green-500" />
                </div>
                <div>
                  <p className="text-xs text-textMuted uppercase font-semibold">{text.status.label}</p>
                  <p className="text-sm font-bold text-green-400">{text.status.value}</p>
                </div>
              </div>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Why Desktop Section */}
      <section id="why-desktop" className="py-24 bg-surface/30 border-y border-white/5 relative">
        <div className="container mx-auto px-6">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={scrollReveal}
            className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-4 text-white">{text.why.title}</h2>
            <p className="text-textMuted max-w-2xl mx-auto">{text.why.subtitle}</p>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {[
              {
                icon: <ShieldCheck className="w-8 h-8 text-primary" />,
                title: text.why.privacyTitle,
                desc: text.why.privacyDesc
              },
              {
                icon: <Zap className="w-8 h-8 text-secondary" />,
                title: text.why.speedTitle,
                desc: text.why.speedDesc
              },
              {
                icon: <WifiOff className="w-8 h-8 text-green-500" />,
                title: text.why.offlineTitle,
                desc: text.why.offlineDesc
              }
            ].map((feature, idx) => (
              <motion.div
                key={idx}
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true, margin: "-50px" }}
                variants={{
                  hidden: { opacity: 0, y: 30 },
                  visible: { opacity: 1, y: 0, transition: { delay: idx * 0.2, duration: 0.5 } }
                }}
                whileHover={{ y: -5 }}
                className="bg-background/50 border border-white/5 p-8 rounded-2xl hover:border-primary/50 transition-colors group shadow-lg shadow-black/20"
              >
                <div className="bg-surface w-16 h-16 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">{feature.title}</h3>
                <p className="text-textMuted leading-relaxed">{feature.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Bento Grid Features */}
      <section id="features" className="py-24 bg-background">
        <div className="container mx-auto px-6">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={scrollReveal}
            className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-4 text-white">{text.features.title}</h2>
            <p className="text-textMuted">{text.features.subtitle}</p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-6xl mx-auto">
            {/* Feature 1: Large Dashboard */}
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6 }}
              className="md:col-span-2 bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-white/10 transition-colors"
            >
              <div className="max-w-xs relative z-10">
                <div className="w-12 h-12 bg-primary/20 rounded-xl flex items-center justify-center mb-4">
                  <Monitor className="w-6 h-6 text-primary" />
                </div>
                <h3 className="text-2xl font-bold mb-2 text-white">{text.features.dashboardTitle}</h3>
                <p className="text-textMuted">{text.features.dashboardDesc}</p>
              </div>
              <div className="absolute right-0 bottom-0 w-2/3 h-2/3 bg-gradient-to-tl from-primary/10 to-transparent rounded-tl-3xl translate-x-10 translate-y-10 group-hover:translate-x-5 group-hover:translate-y-5 transition-transform duration-500">
                <div className="w-full h-full border-l border-t border-white/10 bg-background/50 backdrop-blur-sm rounded-tl-xl p-6">
                  <div className="flex gap-4 mb-4">
                    <div className="h-20 w-1 bg-primary/50 rounded-full" />
                    <div className="h-32 w-1 bg-secondary/50 rounded-full" />
                    <div className="h-16 w-1 bg-green-500/50 rounded-full" />
                  </div>
                  <div className="space-y-2">
                    <div className="h-2 w-3/4 bg-white/10 rounded" />
                    <div className="h-2 w-1/2 bg-white/10 rounded" />
                  </div>
                </div>
              </div>
            </motion.div>

            {/* Feature 2: POS */}
            <motion.div
              initial={{ opacity: 0, y: -50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-emerald-500/30 transition-colors"
            >
              <div className="relative z-10">
                <ShoppingCart className="w-8 h-8 text-emerald-400 mb-4" />
                <h3 className="text-xl font-bold mb-2 text-white">{text.features.posTitle}</h3>
                <p className="text-sm text-textMuted">{text.features.posDesc}</p>
              </div>
              <div className="absolute -right-10 -bottom-10 w-32 h-32 bg-emerald-500/10 rounded-full blur-3xl group-hover:bg-emerald-500/20 transition-colors" />
            </motion.div>

            {/* Feature 3: Purchase Orders */}
            <motion.div
              initial={{ opacity: 0, y: 50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.3 }}
              className="bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-purple-500/30 transition-colors"
            >
              <div className="relative z-10">
                <ClipboardList className="w-8 h-8 text-purple-400 mb-4" />
                <h3 className="text-xl font-bold mb-2 text-white">{text.features.poTitle}</h3>
                <p className="text-sm text-textMuted">{text.features.poDesc}</p>
              </div>
              <div className="absolute -right-10 -bottom-10 w-32 h-32 bg-purple-500/10 rounded-full blur-3xl group-hover:bg-purple-500/20 transition-colors" />
            </motion.div>

            {/* Feature 4: Reports (large) */}
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.4 }}
              className="md:col-span-2 bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-orange-500/20 transition-colors"
            >
              <div className="max-w-md relative z-10">
                <div className="w-12 h-12 bg-orange-500/20 rounded-xl flex items-center justify-center mb-4">
                  <TrendingUp className="w-6 h-6 text-orange-400" />
                </div>
                <h3 className="text-2xl font-bold mb-2 text-white">{text.features.reportsTitle}</h3>
                <p className="text-textMuted">{text.features.reportsDesc}</p>
              </div>
              <div className="absolute right-0 bottom-0 w-1/2 h-2/3 bg-gradient-to-tl from-orange-500/10 to-transparent rounded-tl-3xl translate-x-10 translate-y-10 group-hover:translate-x-5 group-hover:translate-y-5 transition-transform duration-500" />
            </motion.div>

            {/* Feature 5: Smart Alerts */}
            <motion.div
              initial={{ opacity: 0, y: -50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.5 }}
              className="bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-secondary/30 transition-colors"
            >
              <div className="relative z-10">
                <BellRing className="w-8 h-8 text-secondary mb-4" />
                <h3 className="text-xl font-bold mb-2 text-white">{text.features.alertsTitle}</h3>
                <p className="text-sm text-textMuted">{text.features.alertsDesc}</p>
              </div>
              <div className="absolute -right-10 -bottom-10 w-32 h-32 bg-secondary/10 rounded-full blur-3xl group-hover:bg-secondary/20 transition-colors" />
            </motion.div>

            {/* Feature 6: Export */}
            <motion.div
              initial={{ opacity: 0, y: 50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.6 }}
              className="bg-surface/40 border border-white/5 rounded-3xl p-8 relative overflow-hidden group hover:border-green-500/30 transition-colors"
            >
              <div className="relative z-10">
                <FileOutput className="w-8 h-8 text-green-500 mb-4" />
                <h3 className="text-xl font-bold mb-2 text-white">{text.features.exportTitle}</h3>
                <p className="text-sm text-textMuted">{text.features.exportDesc}</p>
              </div>
            </motion.div>

            {/* Feature 7: Search */}
            <motion.div
              initial={{ opacity: 0, x: 50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.7 }}
              className="bg-surface/40 border border-white/5 rounded-3xl p-8 flex items-center gap-4 group hover:bg-surface/60 transition-colors"
            >
              <Search className="w-8 h-8 text-blue-400" />
              <div>
                <h3 className="text-lg font-bold text-white">{text.features.searchTitle}</h3>
                <p className="text-xs text-textMuted">{text.features.searchDesc}</p>
              </div>
            </motion.div>

            {/* Feature 8: Bilingual i18n */}
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: 0.8 }}
              className="md:col-span-2 bg-surface/40 border border-white/5 rounded-3xl p-8 flex items-center gap-6 group hover:bg-surface/60 transition-colors"
            >
              <div className="w-14 h-14 bg-cyan-500/20 rounded-xl flex items-center justify-center flex-shrink-0">
                <Languages className="w-7 h-7 text-cyan-400" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-white mb-1">{text.features.i18nTitle}</h3>
                <p className="text-sm text-textMuted">{text.features.i18nDesc}</p>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Setup Steps */}
      <section id="start" className="py-24 bg-gradient-to-b from-surface/20 to-background border-t border-white/5">
        <div className="container mx-auto px-6 max-w-4xl">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-4 text-white">{text.steps.title}</h2>
            <p className="text-textMuted">{text.steps.subtitle}</p>
          </div>

          <div className="space-y-8">
            {[
              { step: "01", title: text.steps.s1Title, desc: text.steps.s1Desc },
              { step: "02", title: text.steps.s2Title, desc: text.steps.s2Desc },
              { step: "03", title: text.steps.s3Title, desc: text.steps.s3Desc }
            ].map((item, i) => (
              <motion.div
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.2, duration: 0.5 }}
                key={i}
                className="flex items-start gap-6 p-6 rounded-2xl hover:bg-white/5 transition-colors border border-transparent hover:border-white/5"
              >
                <span className="text-5xl font-bold text-white/5 font-mono">{item.step}</span>
                <div>
                  <h3 className="text-xl font-bold mb-2 text-primary">{item.title}</h3>
                  <p className="text-textMuted">{item.desc}</p>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Creator & Footer */}
      <footer className="py-16 bg-surface border-t border-white/5 mt-10">
        <div className="container mx-auto px-6">
          <div className="flex flex-col md:flex-row justify-between items-center gap-10">

            {/* Brand */}
            <div className="text-center md:text-left">
              <div className="flex items-center justify-center md:justify-start gap-2 mb-4">
                <BarChart3 className="w-6 h-6 text-primary" />
                <span className="text-xl font-bold text-white">StockMaster</span>
              </div>
              <p className="text-textMuted text-sm max-w-xs">
                {text.footer.desc}
              </p>
            </div>

            {/* Creator Bio */}
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              className="bg-background/50 p-6 rounded-xl border border-white/5 max-w-sm"
            >
              <div className="flex items-center gap-4 mb-3">
                <div className="w-12 h-12 bg-gradient-to-br from-primary to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-lg shadow-purple-500/20">
                  IL
                </div>
                <div>
                  <p className="text-sm text-textMuted uppercase font-semibold text-xs tracking-wider">{text.footer.createdBy}</p>
                  <a href="https://www.linkedin.com/in/ignaciolegui/" target="_blank" rel="noopener noreferrer" className="font-bold text-white hover:text-primary transition-colors flex items-center gap-2 group">
                    {text.footer.role}
                    <Linkedin className="w-4 h-4 text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                  </a>
                </div>
              </div>
              <p className="text-sm text-textMuted italic">{text.footer.quote}</p>
            </motion.div>

            {/* Links */}
            <div className="flex gap-4">
              <a href="https://github.com/IgnacioLegui/StockMaster" target="_blank" rel="noopener noreferrer" className="p-3 bg-white/5 rounded-lg hover:bg-white/10 transition-colors text-textMuted hover:text-white">
                <Github className="w-5 h-5" />
              </a>
              <a href="https://www.linkedin.com/in/ignaciolegui/" target="_blank" rel="noopener noreferrer" className="p-3 bg-white/5 rounded-lg hover:bg-white/10 transition-colors text-textMuted hover:text-blue-400">
                <Linkedin className="w-5 h-5" />
              </a>
            </div>
          </div>

          <div className="border-t border-white/5 mt-12 pt-8 text-center md:text-left flex flex-col md:flex-row justify-between items-center text-sm text-textMuted">
            <p>&copy; {text.footer.openSource}</p>
            <div className="flex items-center gap-2 mt-4 md:mt-0">
              <span>{text.footer.star}</span>
              <a href="https://github.com/IgnacioLegui/StockMaster" target="_blank" rel="noopener noreferrer" className="text-yellow-400 hover:text-yellow-300 flex items-center gap-1 font-medium transition-colors">
                <Zap className="w-4 h-4 fill-current" /> Star en GitHub
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
