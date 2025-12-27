import { Component, Input, OnChanges, SimpleChanges, OnInit } from '@angular/core';
// Force rebuild
import { CommonModule } from '@angular/common';
import { NgxChartsModule, Color, ScaleType } from '@swimlane/ngx-charts';
import { ApiService } from '../../services/api';
import { Router } from '@angular/router';

@Component({
  selector: 'app-analysis',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './analysis.html',
  styleUrls: ['./analysis.css']
})
export class AnalysisComponent implements OnChanges, OnInit {
  @Input() data: any;

  constructor(private api: ApiService, private router: Router) { }

  ngOnInit() {
    this.api.getProfile().subscribe({
      next: (user) => {
        if (user.role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin']);
        }
      }
    });
  }

  // Chart Data
  revenueByRegion: any[] = [];
  revenueByCategory: any[] = [];
  topProducts: any[] = [];

  // Chart Options
  view: [number, number] = [700, 400];
  gradient: boolean = true;
  showLegend: boolean = true;
  showLabels: boolean = true;
  isDoughnut: boolean = false;

  colorScheme: Color = {
    name: 'modernPro',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: [
      '#4e73df', // Primary Blue
      '#1cc88a', // Success Green
      '#36b9cc', // Info Cyan
      '#f6c23e', // Warning Yellow
      '#e74a3b', // Danger Red
      '#858796', // Secondary Gray
      '#5a5c69', // Dark Gray
      '#6610f2'  // Indigo
    ]
  };

  ngOnChanges(changes: SimpleChanges): void {
    console.log('AnalysisComponent ngOnChanges:', changes);
    if (changes['data'] && this.data) {
      console.log('Processing data:', this.data);
      this.processData();
    }
  }

  getTopClients(): any[] {
    if (!this.data || !this.data.revenue_by_client) return [];
    // Convert object to array and sort by value descending
    return Object.keys(this.data.revenue_by_client)
      .map(key => ({ name: key, value: this.data.revenue_by_client[key] }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 5); // Top 5
  }

  extractedMonthlyData: any[] = [];
  extractedYearlyData: any[] = [];

  getBestProductsPerMonth(): any[] {
    // 1. Try structured data
    if (this.data && this.data.best_product_per_month && Object.keys(this.data.best_product_per_month).length > 0) {
      return Object.keys(this.data.best_product_per_month)
        .map(key => {
          const raw = this.data.best_product_per_month[key];
          const match = raw.match(/^(.*)\s+\((.*)\)$/);
          return {
            date: key,
            product: match ? match[1] : raw,
            value: match ? match[2] : ''
          };
        })
        .sort((a, b) => a.date.localeCompare(b.date));
    }

    // 2. Fallback to extracted text data
    return this.extractedMonthlyData;
  }

  getBestProductsPerYear(): any[] {
    // 1. Try structured data
    if (this.data && this.data.best_product_per_year && Object.keys(this.data.best_product_per_year).length > 0) {
      return Object.keys(this.data.best_product_per_year)
        .map(key => {
          const raw = this.data.best_product_per_year[key];
          const match = raw.match(/^(.*)\s+\((.*)\)$/);
          return {
            year: key,
            product: match ? match[1] : raw,
            value: match ? match[2] : ''
          };
        })
        .sort((a, b) => a.year.localeCompare(b.year));
    }

    // 2. Fallback to extracted text data
    return this.extractedYearlyData;
  }

  getProductForecasts(): any[] {
    // Assuming forecast_per_product is an object or list. 
    // Based on user text: "- ProductID: Value (Variation)"
    // If it's a simple object {ID: Value}, we map it. 
    // If it's a complex object, we might need to adjust.
    if (!this.data || !this.data.forecast_per_product) return [];

    // Check if it's an array or object
    if (Array.isArray(this.data.forecast_per_product)) {
      return this.data.forecast_per_product;
    }

    return Object.keys(this.data.forecast_per_product)
      .map(key => ({ name: key, value: this.data.forecast_per_product[key] }));
  }

  getStockRisk(): any[] {
    if (!this.data || !this.data.stock_risk) return [];
    if (Array.isArray(this.data.stock_risk)) return this.data.stock_risk;

    // If it's an object { "Product": "Risk Level" } or { "Product": "Reason" }
    return Object.keys(this.data.stock_risk).map(key => ({
      product: key,
      status: this.data.stock_risk[key]
    }));
  }

  getWinningProduct(): string {
    return this.data?.winning_product || 'N/A';
  }

  insightCards: { title: string, content: string }[] = [];

  onSelect(data: any): void {
    console.log('Item clicked', JSON.parse(JSON.stringify(data)));
  }

  processData() {
    console.log('Starting processData with:', this.data);
    // Process Revenue by Region
    if (this.data.revenue_by_region) {
      this.revenueByRegion = Object.keys(this.data.revenue_by_region).map(key => ({
        name: key,
        value: this.data.revenue_by_region[key]
      }));
    }

    // Process Revenue by Category
    if (this.data.revenue_by_category) {
      this.revenueByCategory = Object.keys(this.data.revenue_by_category).map(key => ({
        name: key,
        value: this.data.revenue_by_category[key]
      }));
    }

    this.parseInsights();
  }

  parseInsights() {
    if (!this.data || !this.data.analysis_text) {
      this.insightCards = [];
      return;
    }

    // Reset extracted data
    this.extractedMonthlyData = [];
    this.extractedYearlyData = [];

    // Split by double newlines to separate sections
    const blocks = this.data.analysis_text.split(/\n\n+/);

    this.insightCards = blocks.map((block: string) => {
      const lines = block.trim().split('\n');
      let title = 'Insight';
      let content = block;

      // Heuristic: If first line is short and looks like a header
      if (lines.length > 0 && lines[0].length < 60 && lines[0].length > 2) {
        title = lines[0].trim();
        content = lines.slice(1).join('\n').trim();
      }

      // If content is empty (e.g. just a header in the block), keep original
      if (!content) {
        content = title;
        title = 'Note';
      }

      // Check for Special Data Blocks to Extract
      const lowerTitle = title.toLowerCase();

      // 1. Best Product per Month
      if (lowerTitle.includes('best product per month') || lowerTitle.includes('meilleur produit par mois')) {
        this.extractedMonthlyData = lines.slice(1).map(line => {
          // Format: "2014-01: FUR-CH-10004063 (2,573.82)"
          const match = line.match(/^(\d{4}-\d{2}):\s+(.*)\s+\((.*)\)$/);
          if (match) {
            return { date: match[1], product: match[2], value: match[3] };
          }
          return null;
        }).filter(item => item !== null);
        return null; // Exclude from cards
      }

      // 2. Best Product per Year
      if (lowerTitle.includes('best product per year') || lowerTitle.includes('meilleur produit par année')) {
        this.extractedYearlyData = lines.slice(1).map(line => {
          // Format: "2014: FUR-CH-10004063 (2,573.82)"
          const match = line.match(/^(\d{4}):\s+(.*)\s+\((.*)\)$/);
          if (match) {
            return { year: match[1], product: match[2], value: match[3] };
          }
          return null;
        }).filter(item => item !== null);
        return null; // Exclude from cards
      }

      // 3. Filter out sections already displayed in the dashboard
      if (
        lowerTitle.includes('key performance indicators') ||
        lowerTitle.includes('kpis') ||
        lowerTitle.includes('sales forecast') ||
        lowerTitle.includes('winning product') ||
        lowerTitle.includes('revenue by region') ||
        lowerTitle.includes('revenue by category') ||
        lowerTitle.includes('top performing products') ||
        lowerTitle.includes('best selling products') ||
        lowerTitle.includes('top clients')
      ) {
        return null;
      }

      return { title, content };
    }).filter((card: any) => {
      // Filter out nulls (extracted blocks) and unwanted cards
      if (!card) return false;

      return card.content.length > 0 &&
        !card.title.toLowerCase().includes('modèle utilisé') &&
        !card.content.toLowerCase().includes('linearregression');
    });
  }
}
